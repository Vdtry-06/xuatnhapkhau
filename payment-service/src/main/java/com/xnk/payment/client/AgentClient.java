package com.xnk.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "agent-service", url = "${services.agent-service}")
public interface AgentClient {
    @PutMapping("/api/agents/{id}/deduct-credit")
    void deductCredit(@PathVariable("id") Long agentId, @RequestParam("amount") BigDecimal amount);

    @PutMapping("/api/agents/{id}/restore-credit")
    void restoreCredit(@PathVariable("id") Long agentId, @RequestParam("amount") BigDecimal amount);
}
