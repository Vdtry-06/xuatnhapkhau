package com.xnk.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AuthDTO {

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record LoginResponse(
            String token,
            String role,
            Long agentId,
            Long supplierId,
            String username
    ) {}

    /**
     * Admin tạo tài khoản đăng nhập cho sub-agent.
     * name KHÔNG cần truyền vào đây vì đã có trong Agent Service.
     * agentId dùng để liên kết tài khoản với bản ghi agent.
     */
    public record RegisterRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotNull  Long agentId
    ) {}

    public record RegisterSupplierRequest(
            @NotBlank String username,
            @NotBlank String password,
            @NotNull Long supplierId
    ) {}

    public record UserResponse(
            Long id,
            String username,
            String role,
            Long agentId,
            Long supplierId,
            boolean enabled
    ) {}
}
