package com.xnk.agent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AgentDTO {

    public record AgentRequest(
            @NotBlank(message = "Tên đại lý không được để trống")
            String name,

            @NotBlank(message = "Email không được để trống")
            @Email(message = "Email không hợp lệ")
            String email,

            String phone,
            String address,

            @NotBlank(message = "Mã số thuế không được để trống")
            String taxCode
    ) {}

    public record AgentResponse(
            Long id,
            String name,
            String email,
            String phone,
            String address,
            String taxCode,
            String tierLevel,
            java.math.BigDecimal creditLimit,
            String status
    ) {}
}
