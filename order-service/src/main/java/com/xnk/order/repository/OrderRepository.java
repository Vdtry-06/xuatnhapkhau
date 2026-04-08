package com.xnk.order.repository;

import com.xnk.order.entity.Order;
import com.xnk.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByAgentId(Long agentId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findBySupplierId(Long supplierId);
}
