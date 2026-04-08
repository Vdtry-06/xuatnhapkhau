package com.xnk.agent.repository;

import com.xnk.agent.entity.Agent;
import com.xnk.agent.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {

    Optional<Agent> findByEmail(String email);

    Optional<Agent> findByTaxCode(String taxCode);

    List<Agent> findByStatus(Status status);
}
