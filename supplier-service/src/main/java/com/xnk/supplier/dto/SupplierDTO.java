package com.xnk.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class SupplierDTO {

    public record SupplierRequest(
            @NotBlank(message = "Tên nhà cung cấp không được để trống")
            String name,

            @NotBlank(message = "Email không được để trống")
            @Email(message = "Email không hợp lệ")
            String email,

            String phone,
            String address,

            @NotBlank(message = "Mã số thuế không được để trống")
            String taxCode
    ) {}

    public record SupplierResponse(
            Long id,
            String name,
            String email,
            String phone,
            String address,
            String taxCode,
            String status
    ) {}
}
