package com.xnk.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "agent-service", url = "${services.agent-service}")
public interface AgentClient {

    @GetMapping("/api/agents/{id}/check-credit")
    boolean checkCreditLimit(@PathVariable("id") Long id, @RequestParam("orderAmount") BigDecimal orderAmount);

    @GetMapping("/api/agents/{id}")
    AgentResponse getAgentById(@PathVariable("id") Long id);

    record AgentResponse(
            Long id,
            String status
    ) {}
}
