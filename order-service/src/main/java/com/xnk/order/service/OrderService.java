package com.xnk.order.service;

import com.xnk.order.client.AgentClient;
import com.xnk.order.client.ProductClient;
import com.xnk.order.dto.OrderDTO.*;
import com.xnk.order.entity.Order;
import com.xnk.order.entity.Orderline;
import com.xnk.order.entity.OrderlineSupplierResponse;
import com.xnk.order.enums.OrderStatus;
import com.xnk.order.enums.SupplierResponseStatus;
import com.xnk.order.repository.OrderRepository;
import com.xnk.order.repository.OrderlineRepository;
import com.xnk.order.repository.OrderlineSupplierResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderlineRepository orderlineRepository;
    private final OrderlineSupplierResponseRepository orderlineSupplierResponseRepository;
    private final AgentClient agentClient;
    private final ProductClient productClient;

    public OrderService(OrderRepository orderRepository,
                        OrderlineRepository orderlineRepository,
                        OrderlineSupplierResponseRepository orderlineSupplierResponseRepository,
                        AgentClient agentClient,
                        ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.orderlineRepository = orderlineRepository;
        this.orderlineSupplierResponseRepository = orderlineSupplierResponseRepository;
        this.agentClient = agentClient;
        this.productClient = productClient;
    }

    public OrderResponse createOrder(OrderRequest request) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderlineRequest lineReq : request.orderlines()) {
            ProductClient.ProductResponse product;
            try {
                product = productClient.getProductById(lineReq.productId());
            } catch (feign.FeignException.NotFound e) {
                throw new IllegalArgumentException("Sản phẩm với id=" + lineReq.productId() + " không tồn tại!");
            } catch (Exception e) {
                throw new RuntimeException("Không thể kiểm tra sản phẩm id=" + lineReq.productId() + " lúc này.");
            }

            totalAmount = totalAmount.add(product.exportPrice().multiply(BigDecimal.valueOf(lineReq.quantity())));
        }

        try {
            agentClient.getAgentById(request.agentId());
        } catch (Exception e) {
            throw new IllegalArgumentException("Không tìm thấy đại lý id=" + request.agentId());
        }

        Order order = Order.builder()
                .agentId(request.agentId())
                .orderDate(LocalDateTime.now())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.shippingAddress())
                .build();

        Order savedOrder = orderRepository.save(order);

        List<Orderline> orderlines = new ArrayList<>();
        for (OrderlineRequest lineReq : request.orderlines()) {
            ProductClient.ProductResponse product = productClient.getProductById(lineReq.productId());
            BigDecimal subTotal = product.exportPrice().multiply(BigDecimal.valueOf(lineReq.quantity()));

            Orderline line = Orderline.builder()
                    .order(savedOrder)
                    .productId(lineReq.productId())
                    .productName(product.name())
                    .quantity(lineReq.quantity())
                    .unitPrice(product.exportPrice())
                    .subTotal(subTotal)
                    .build();
            orderlines.add(line);
        }
        orderlineRepository.saveAll(orderlines);
        savedOrder.setOrderlines(orderlines);

        return toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistoryByAgent(Long agentId) {
        return orderRepository.findByAgentId(agentId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        return toResponse(findOrder(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersBySupplier(Long supplierId) {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELED && order.getStatus() != OrderStatus.DELIVERED)
                .filter(order -> isRelevantForSupplier(order, supplierId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).toList();
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = findOrder(orderId);
        validateStatusTransition(order.getStatus(), status);
        order.setStatus(status);
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse updateOrderStatusBySupplier(Long orderId, Long supplierId, OrderStatus status) {
        Order order = findOrder(orderId);
        boolean supplierSelected = order.getOrderlines().stream()
                .anyMatch(line -> Objects.equals(line.getSupplierId(), supplierId));
        if (!supplierSelected) {
            throw new IllegalArgumentException("Đơn hàng này không thuộc nhà cung cấp id=" + supplierId);
        }
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalArgumentException("Nhà cung cấp chỉ được cập nhật đơn hàng sang trạng thái SHIPPED.");
        }
        validateStatusTransition(order.getStatus(), status);
        order.setStatus(status);
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse confirmDeliveredByAgent(Long orderId, Long agentId) {
        Order order = findOrder(orderId);
        if (!order.getAgentId().equals(agentId)) {
            throw new IllegalArgumentException("Đơn hàng này không thuộc đại lý id=" + agentId);
        }
        validateStatusTransition(order.getStatus(), OrderStatus.DELIVERED);
        order.setStatus(OrderStatus.DELIVERED);
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse respondSupplierForOrderline(Long orderId, Long orderlineId, SupplierOptionRequest request) {
        Order order = findOrder(orderId);
        Orderline orderline = findOrderline(orderId, orderlineId);
        if (orderline.getSupplierId() != null && !Objects.equals(orderline.getSupplierId(), request.supplierId())) {
            throw new IllegalStateException("Agent đã chọn nhà cung cấp khác cho dòng hàng này.");
        }

        SupplierResponseStatus responseStatus;
        try {
            responseStatus = SupplierResponseStatus.valueOf(request.status().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái phản hồi supplier không hợp lệ: " + request.status());
        }

        int totalCanSupply = request.availableQuantity() + request.productionQuantity();
        if (responseStatus != SupplierResponseStatus.CANNOT_FULFILL && totalCanSupply < orderline.getQuantity()) {
            throw new IllegalArgumentException("Supplier chưa đáp ứng đủ số lượng yêu cầu cho line này.");
        }

        OrderlineSupplierResponse response = orderlineSupplierResponseRepository
                .findByOrderlineIdAndSupplierId(orderlineId, request.supplierId())
                .orElseGet(() -> OrderlineSupplierResponse.builder()
                        .orderline(orderline)
                        .supplierId(request.supplierId())
                        .supplierName(request.supplierName())
                        .build());

        response.setSupplierName(request.supplierName());
        response.setAvailableQuantity(request.availableQuantity());
        response.setProductionQuantity(request.productionQuantity());
        response.setExpectedReadyDate(request.expectedReadyDate());
        response.setStatus(responseStatus);
        response.setNote(request.note());
        orderlineSupplierResponseRepository.save(response);

        refreshOrderPlanning(order);
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse selectSupplierResponse(Long orderId, Long orderlineId, Long responseId) {
        Order order = findOrder(orderId);
        Orderline orderline = findOrderline(orderId, orderlineId);
        OrderlineSupplierResponse response = orderlineSupplierResponseRepository.findById(responseId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phản hồi supplier id=" + responseId));

        if (!response.getOrderline().getId().equals(orderlineId)) {
            throw new IllegalArgumentException("Phản hồi supplier không thuộc order line này.");
        }
        if (response.getStatus() == SupplierResponseStatus.CANNOT_FULFILL) {
            throw new IllegalArgumentException("Không thể chọn supplier đã báo không đáp ứng được.");
        }
        if (response.getAvailableQuantity() + response.getProductionQuantity() < orderline.getQuantity()) {
            throw new IllegalArgumentException("Phản hồi supplier chưa đủ số lượng để chọn.");
        }

        orderline.setSupplierId(response.getSupplierId());
        orderline.setSupplierName(response.getSupplierName());
        refreshOrderPlanning(order);
        return toResponse(orderRepository.save(order));
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        boolean valid = switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.SUPPLIER_REVIEWING || newStatus == OrderStatus.CONFIRMED
                    || newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;
            case SUPPLIER_REVIEWING -> newStatus == OrderStatus.PARTIALLY_PLANNED || newStatus == OrderStatus.READY_TO_SHIP
                    || newStatus == OrderStatus.IN_PRODUCTION || newStatus == OrderStatus.UNFULFILLABLE
                    || newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;
            case PARTIALLY_PLANNED -> newStatus == OrderStatus.READY_TO_SHIP || newStatus == OrderStatus.IN_PRODUCTION
                    || newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;
            case READY_TO_SHIP -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;
            case IN_PRODUCTION -> newStatus == OrderStatus.READY_TO_SHIP || newStatus == OrderStatus.CONFIRMED
                    || newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;
            case CONFIRMED -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, UNFULFILLABLE, CANCELED -> false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Không thể chuyển trạng thái đơn hàng từ " + currentStatus + " sang " + newStatus);
        }
    }

    private boolean isRelevantForSupplier(Order order, Long supplierId) {
        boolean assigned = order.getOrderlines().stream()
                .anyMatch(line -> Objects.equals(line.getSupplierId(), supplierId));
        boolean responded = order.getOrderlines().stream()
                .flatMap(line -> line.getSupplierResponses().stream())
                .anyMatch(response -> Objects.equals(response.getSupplierId(), supplierId));
        return assigned || responded || order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.SUPPLIER_REVIEWING;
    }

    private void refreshOrderPlanning(Order order) {
        boolean hasAnyResponse = order.getOrderlines().stream().anyMatch(line -> !line.getSupplierResponses().isEmpty());
        boolean allLinesSelected = order.getOrderlines().stream().allMatch(line -> line.getSupplierId() != null);
        boolean anyLineSelected = order.getOrderlines().stream().anyMatch(line -> line.getSupplierId() != null);

        boolean anyLineInProduction = order.getOrderlines().stream().anyMatch(line -> {
            if (line.getSupplierId() == null) return false;
            return line.getSupplierResponses().stream()
                    .filter(resp -> Objects.equals(resp.getSupplierId(), line.getSupplierId()))
                    .anyMatch(resp -> resp.getProductionQuantity() > 0
                            || resp.getStatus() == SupplierResponseStatus.IN_PRODUCTION
                            || resp.getStatus() == SupplierResponseStatus.PARTIAL_FULFILL
                            || isFutureDate(resp.getExpectedReadyDate()));
        });

        boolean hasFulfillableOptionForEveryLine = order.getOrderlines().stream().allMatch(line ->
                line.getSupplierId() != null || line.getSupplierResponses().stream()
                        .anyMatch(resp -> resp.getStatus() != SupplierResponseStatus.CANNOT_FULFILL
                                && resp.getAvailableQuantity() + resp.getProductionQuantity() >= line.getQuantity()));

        if (!hasAnyResponse) {
            order.setStatus(OrderStatus.PENDING);
        } else if (!hasFulfillableOptionForEveryLine) {
            order.setStatus(OrderStatus.UNFULFILLABLE);
        } else if (!allLinesSelected && anyLineSelected) {
            order.setStatus(OrderStatus.PARTIALLY_PLANNED);
        } else if (!allLinesSelected) {
            order.setStatus(OrderStatus.SUPPLIER_REVIEWING);
        } else if (anyLineInProduction) {
            order.setStatus(OrderStatus.IN_PRODUCTION);
        } else {
            order.setStatus(OrderStatus.READY_TO_SHIP);
        }

        Long singleSupplierId = null;
        String singleSupplierName = null;
        boolean sameSupplier = true;
        for (Orderline line : order.getOrderlines()) {
            if (line.getSupplierId() == null) {
                sameSupplier = false;
                break;
            }
            if (singleSupplierId == null) {
                singleSupplierId = line.getSupplierId();
                singleSupplierName = line.getSupplierName();
            } else if (!singleSupplierId.equals(line.getSupplierId())) {
                sameSupplier = false;
                break;
            }
        }
        order.setSupplierId(sameSupplier ? singleSupplierId : null);
        order.setSupplierName(sameSupplier ? singleSupplierName : null);
    }

    private boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng id: " + orderId));
    }

    private Orderline findOrderline(Long orderId, Long orderlineId) {
        Orderline line = orderlineRepository.findById(orderlineId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy order line id: " + orderlineId));
        if (!line.getOrder().getId().equals(orderId)) {
            throw new IllegalArgumentException("Order line không thuộc đơn hàng id=" + orderId);
        }
        return line;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderlineResponse> lines = order.getOrderlines().stream()
                .map(l -> new OrderlineResponse(
                        l.getId(),
                        l.getProductId(),
                        l.getProductName(),
                        l.getSupplierId(),
                        l.getSupplierName(),
                        l.getQuantity(),
                        l.getUnitPrice(),
                        l.getSubTotal(),
                        l.getSupplierResponses().stream()
                                .map(resp -> new SupplierOptionResponse(
                                        resp.getId(),
                                        resp.getSupplierId(),
                                        resp.getSupplierName(),
                                        resp.getAvailableQuantity(),
                                        resp.getProductionQuantity(),
                                        resp.getExpectedReadyDate(),
                                        resp.getStatus().name(),
                                        resp.getNote(),
                                        Objects.equals(resp.getSupplierId(), l.getSupplierId()),
                                        l.getSupplierId() != null && !Objects.equals(resp.getSupplierId(), l.getSupplierId())))
                                .toList()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getAgentId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getSupplierId(),
                order.getSupplierName(),
                order.getShippingAddress(),
                lines);
    }
}
