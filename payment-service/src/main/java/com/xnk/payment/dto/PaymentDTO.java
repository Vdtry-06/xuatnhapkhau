package com.xnk.payment.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDTO {

    public record PaymentRequest(
            @NotNull(message = "orderId không được để trống")
            Long orderId,

            @NotNull(message = "Số tiền không được để trống")
            @DecimalMin(value = "0.0", inclusive = false, message = "Số tiền phải lớn hơn 0")
            BigDecimal amount,

            @NotBlank(message = "Phương thức thanh toán không được để trống")
            String paymentMethod
    ) {}

    public record PaymentResponse(
            Long id,
            Long orderId,
            BigDecimal amount,
            String paymentMethod,
            LocalDateTime paymentDate,
            String status,
            String transactionRef
    ) {}
}
