package com.xnk.order.controller;

import com.xnk.order.dto.OrderDTO.*;
import com.xnk.order.enums.OrderStatus;
import com.xnk.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<OrderResponse>> getOrderHistoryByAgent(@PathVariable("agentId") Long agentId) {
        return ResponseEntity.ok(orderService.getOrderHistoryByAgent(agentId));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<OrderResponse>> getOrdersBySupplier(@PathVariable("supplierId") Long supplierId) {
        return ResponseEntity.ok(orderService.getOrdersBySupplier(supplierId));
    }

    @PutMapping("/{id}/supplier/{supplierId}/ship")
    public ResponseEntity<OrderResponse> shipOrderBySupplier(
            @PathVariable("id") Long id,
            @PathVariable("supplierId") Long supplierId) {
        return ResponseEntity.ok(orderService.updateOrderStatusBySupplier(id, supplierId, OrderStatus.SHIPPED));
    }

    @PutMapping("/{id}/agent/{agentId}/deliver")
    public ResponseEntity<OrderResponse> confirmDeliveredByAgent(
            @PathVariable("id") Long id,
            @PathVariable("agentId") Long agentId) {
        return ResponseEntity.ok(orderService.confirmDeliveredByAgent(id, agentId));
    }

    @PutMapping("/{id}/lines/{lineId}/supplier-response")
    public ResponseEntity<OrderResponse> supplierRespondForLine(
            @PathVariable("id") Long id,
            @PathVariable("lineId") Long lineId,
            @Valid @RequestBody SupplierOptionRequest request) {
        return ResponseEntity.ok(orderService.respondSupplierForOrderline(id, lineId, request));
    }

    @PutMapping("/{id}/lines/{lineId}/responses/{responseId}/select")
    public ResponseEntity<OrderResponse> selectSupplierResponse(
            @PathVariable("id") Long id,
            @PathVariable("lineId") Long lineId,
            @PathVariable("responseId") Long responseId) {
        return ResponseEntity.ok(orderService.selectSupplierResponse(id, lineId, responseId));
    }
}
