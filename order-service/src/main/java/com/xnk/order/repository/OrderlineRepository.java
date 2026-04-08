package com.xnk.order.repository;

import com.xnk.order.entity.Orderline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderlineRepository extends JpaRepository<Orderline, Long> {

    List<Orderline> findByOrderId(Long orderId);
}
