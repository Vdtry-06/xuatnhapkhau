package com.xnk.payment.service;

import com.xnk.payment.client.AgentClient;
import com.xnk.payment.client.OrderClient;
import com.xnk.payment.dto.PaymentDTO.*;
import com.xnk.payment.entity.Payment;
import com.xnk.payment.enums.PaymentMethod;
import com.xnk.payment.enums.PaymentStatus;
import com.xnk.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final AgentClient agentClient;

    public PaymentService(PaymentRepository paymentRepository, OrderClient orderClient, AgentClient agentClient) {
        this.paymentRepository = paymentRepository;
        this.orderClient = orderClient;
        this.agentClient = agentClient;
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        OrderClient.OrderResponse orderResponse;
        try {
            orderResponse = orderClient.getOrderById(request.orderId());
        } catch (feign.FeignException.BadRequest e) {
            throw new IllegalArgumentException("Giao dịch thất bại: Đơn hàng id=" + request.orderId() + " không tồn tại!");
        } catch (Exception e) {
            throw new RuntimeException("Hệ thống gián đoạn, không thể kiểm tra đơn hàng lúc này: " + e.getMessage());
        }

        if ("CANCELED".equals(orderResponse.status())) {
            throw new IllegalArgumentException("Đơn hàng đã bị hủy, không thể thanh toán.");
        }

        if (request.amount().compareTo(orderResponse.totalAmount()) != 0) {
            throw new IllegalArgumentException("Số tiền thanh toán " + request.amount() +
                    " không khớp với tổng giá trị đơn hàng " + orderResponse.totalAmount());
        }

        if (paymentRepository.findByOrderId(request.orderId()).isPresent()) {
            throw new IllegalArgumentException("Đơn hàng id=" + request.orderId() + " đã được thanh toán.");
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.paymentMethod().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Phương thức thanh toán không hợp lệ: " + request.paymentMethod() +
                    "Chấp nhận: BANK_TRANSFER, CREDIT_DEBT, LC");
        }

        PaymentStatus initialStatus = PaymentStatus.SUCCESS;
        if (method == PaymentMethod.CREDIT_DEBT) {
            initialStatus = PaymentStatus.PENDING;
            try {
                agentClient.deductCredit(orderResponse.agentId(), request.amount());
            } catch (feign.FeignException.BadRequest e) {
                throw new IllegalArgumentException("Không thể ghi nợ: Đại lý không đủ hạn mức tín dụng");
            } catch (Exception e) {
                throw new RuntimeException("Lỗi kết nối đến hệ thống Đại lý khi trừ hạn mức");
            }
        } else if (method == PaymentMethod.LC) {
            initialStatus = PaymentStatus.PENDING;
        }

        String transactionRef = generateTransactionRef(method);

        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .amount(request.amount())
                .paymentMethod(method)
                .paymentDate(LocalDateTime.now())
                .status(initialStatus)
                .transactionRef(transactionRef)
                .build();

        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = findPayment(paymentId);
        validatePaymentStatusTransition(payment.getStatus(), status);
        OrderClient.OrderResponse orderResponse = orderClient.getOrderById(payment.getOrderId());

        if (status == PaymentStatus.FAILED
                && payment.getStatus() == PaymentStatus.PENDING
                && payment.getPaymentMethod() == PaymentMethod.CREDIT_DEBT) {
            restoreCredit(orderResponse.agentId(), payment.getAmount());
        }

        payment.setStatus(status);

        if (status == PaymentStatus.SUCCESS) {
            payment.setPaymentDate(LocalDateTime.now());
        }

        return toResponse(paymentRepository.save(payment));
    }

    public PaymentResponse updatePaymentStatusBySupplier(Long paymentId, Long supplierId, PaymentStatus status) {
        Payment payment = findPayment(paymentId);
        OrderClient.OrderResponse orderResponse = orderClient.getOrderById(payment.getOrderId());
        boolean supplierParticipating = orderResponse.orderlines() != null
                && orderResponse.orderlines().stream().anyMatch(line -> supplierId.equals(line.supplierId()));
        if (!supplierParticipating) {
            throw new IllegalArgumentException("Thanh toán này không thuộc nhà cung cấp id=" + supplierId);
        }
        if (status == PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Supplier chỉ được cập nhật SUCCESS hoặc FAILED.");
        }
        return updatePaymentStatus(paymentId, status);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán cho đơn hàng id: " + orderId));
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {
        return toResponse(findPayment(paymentId));
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    private void validatePaymentStatusTransition(PaymentStatus currentStatus, PaymentStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case PENDING -> newStatus == PaymentStatus.SUCCESS || newStatus == PaymentStatus.FAILED;
            case SUCCESS, FAILED -> false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Không thể chuyển trạng thái thanh toán từ " + currentStatus + " sang " + newStatus);
        }
    }

    private void restoreCredit(Long agentId, java.math.BigDecimal amount) {
        try {
            agentClient.restoreCredit(agentId, amount);
        } catch (Exception e) {
            throw new RuntimeException("Cập nhật thanh toán thất bại do lỗi hoàn hạn mức: " + e.getMessage());
        }
    }

    private String generateTransactionRef(PaymentMethod method) {
        String prefix = switch (method) {
            case BANK_TRANSFER -> "BT";
            case CREDIT_DEBT   -> "CD";
            case LC            -> "LC";
        };
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Payment findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán id: " + paymentId));
    }

    private PaymentResponse toResponse(Payment p) {
        return new PaymentResponse(
                p.getId(),
                p.getOrderId(),
                p.getAmount(),
                p.getPaymentMethod().name(),
                p.getPaymentDate(),
                p.getStatus().name(),
                p.getTransactionRef()
        );
    }
}
