package com.xnk.auth.controller;

import com.xnk.auth.dto.AuthDTO.*;
import com.xnk.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // Admin tạo tài khoản cho đại lý con
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/register-supplier")
    public ResponseEntity<UserResponse> registerSupplier(@Valid @RequestBody RegisterSupplierRequest req) {
        return ResponseEntity.ok(authService.registerSupplier(req));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(authService.getAllUsers());
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<UserResponse> toggleUser(@PathVariable Long id) {
        return ResponseEntity.ok(authService.toggleEnabled(id));
    }
}
