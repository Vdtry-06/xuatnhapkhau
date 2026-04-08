package com.xnk.agent.controller;

import com.xnk.agent.dto.AgentDTO.AgentRequest;
import com.xnk.agent.dto.AgentDTO.AgentResponse;
import com.xnk.agent.enums.Status;
import com.xnk.agent.service.AgentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    public ResponseEntity<AgentResponse> createAgent(@Valid @RequestBody AgentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agentService.createAgent(request));
    }

    @GetMapping
    public ResponseEntity<List<AgentResponse>> getAllAgents() {
        return ResponseEntity.ok(agentService.getAllAgents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentResponse> getAgentById(@PathVariable Long id) {
        return ResponseEntity.ok(agentService.getAgentById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AgentResponse> updateAgentStatus(
            @PathVariable Long id, @RequestParam Status status) {
        return ResponseEntity.ok(agentService.updateAgentStatus(id, status));
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<AgentResponse>> getByStatus(@RequestParam Status status) {
        return ResponseEntity.ok(agentService.getAgentsByStatus(status));
    }

    @GetMapping("/{id}/check-credit")
    public ResponseEntity<Boolean> checkCreditLimit(
            @PathVariable Long id, @RequestParam BigDecimal orderAmount) {
        return ResponseEntity.ok(agentService.checkCreditLimit(id, orderAmount));
    }

    /** Internal — Payment Service gọi khi CREDIT_DEBT: trừ hạn mức */
    @PutMapping("/{id}/deduct-credit")
    public ResponseEntity<Void> deductCredit(
            @PathVariable Long id, @RequestParam BigDecimal amount) {
        agentService.deductCredit(id, amount);
        return ResponseEntity.ok().build();
    }

    /** Internal — Payment Service gọi khi thanh toán SUCCESS: cộng lại hạn mức */
    @PutMapping("/{id}/restore-credit")
    public ResponseEntity<Void> restoreCredit(
            @PathVariable Long id, @RequestParam BigDecimal amount) {
        agentService.restoreCredit(id, amount);
        return ResponseEntity.ok().build();
    }
}
