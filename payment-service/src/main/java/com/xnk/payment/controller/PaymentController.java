package com.xnk.payment.controller;

import com.xnk.payment.dto.PaymentDTO.*;
import com.xnk.payment.enums.PaymentStatus;
import com.xnk.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.processPayment(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") PaymentStatus status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, status));
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<PaymentResponse>> getByStatus(@RequestParam("status") PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @PutMapping("/{id}/supplier/{supplierId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatusBySupplier(
            @PathVariable("id") Long id,
            @PathVariable("supplierId") Long supplierId,
            @RequestParam("status") PaymentStatus status) {
        return ResponseEntity.ok(paymentService.updatePaymentStatusBySupplier(id, supplierId, status));
    }
}
