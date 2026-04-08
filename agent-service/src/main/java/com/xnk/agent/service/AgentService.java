package com.xnk.agent.service;

import com.xnk.agent.dto.AgentDTO.AgentRequest;
import com.xnk.agent.dto.AgentDTO.AgentResponse;
import com.xnk.agent.entity.Agent;
import com.xnk.agent.enums.Status;
import com.xnk.agent.enums.TierLevel;
import com.xnk.agent.repository.AgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class AgentService {

    private final AgentRepository agentRepository;

    public AgentService(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    public AgentResponse createAgent(AgentRequest request) {
        if (agentRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại: " + request.email());
        }
        if (agentRepository.findByTaxCode(request.taxCode()).isPresent()) {
            throw new IllegalArgumentException("Mã số thuế đã tồn tại: " + request.taxCode());
        }

        Agent agent = Agent.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .taxCode(request.taxCode())
                .status(Status.INACTIVE)
                .tierLevel(TierLevel.BRONZE)
                .creditLimit(BigDecimal.valueOf(50_000_000))
                .build();

        return toResponse(agentRepository.save(agent));
    }

    public AgentResponse updateAgentStatus(Long agentId, Status status) {
        Agent agent = findAgent(agentId);
        agent.setStatus(status);
        return toResponse(agentRepository.save(agent));
    }

    @Transactional(readOnly = true)
    public AgentResponse getAgentById(Long agentId) {
        return toResponse(findAgent(agentId));
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> getAllAgents() {
        return agentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AgentResponse> getAgentsByStatus(Status status) {
        return agentRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public boolean checkCreditLimit(Long agentId, BigDecimal orderAmount) {
        Agent agent = findAgent(agentId);
        if (agent.getStatus() != Status.ACTIVE) return false;
        return agent.getCreditLimit().compareTo(orderAmount) >= 0;
    }

    /**
     * Trừ hạn mức — gọi khi thanh toán bằng CREDIT_DEBT (ghi nợ).
     * Đại lý dùng hạn mức để nợ trước, trả sau.
     */
    public void deductCredit(Long agentId, BigDecimal amount) {
        Agent agent = findAgent(agentId);
        if (agent.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("Đại lý đang bị khóa hoặc không hoạt động.");
        }
        if (agent.getCreditLimit().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Đại lý không đủ hạn mức tín dụng. Hạn mức: "
                    + agent.getCreditLimit() + ", Yêu cầu: " + amount);
        }
        agent.setCreditLimit(agent.getCreditLimit().subtract(amount));
        agentRepository.save(agent);
        System.out.println("[AGENT] Trừ hạn mức " + amount + " | Đại lý #" + agentId
                + " | Còn lại: " + agent.getCreditLimit());
    }

    /**
     * Cộng lại hạn mức — gọi khi thanh toán SUCCESS.
     * Đại lý đã thanh toán xong → hạn mức được giải phóng để dùng cho đơn tiếp theo.
     */
    public void restoreCredit(Long agentId, BigDecimal amount) {
        Agent agent = findAgent(agentId);
        agent.setCreditLimit(agent.getCreditLimit().add(amount));
        agentRepository.save(agent);
        System.out.println("[AGENT] Hoàn hạn mức " + amount + " | Đại lý #" + agentId
                + " | Mới: " + agent.getCreditLimit());
    }

    private Agent findAgent(Long agentId) {
        return agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đại lý id: " + agentId));
    }

    private AgentResponse toResponse(Agent agent) {
        return new AgentResponse(
                agent.getId(), agent.getName(), agent.getEmail(), agent.getPhone(),
                agent.getAddress(), agent.getTaxCode(), agent.getTierLevel().name(),
                agent.getCreditLimit(), agent.getStatus().name()
        );
    }
}
