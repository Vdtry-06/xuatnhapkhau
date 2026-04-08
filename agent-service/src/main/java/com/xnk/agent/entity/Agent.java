package com.xnk.agent.entity;

import com.xnk.agent.enums.Status;
import com.xnk.agent.enums.TierLevel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "agents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String address;

    @Column(unique = true)
    private String taxCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TierLevel tierLevel = TierLevel.BRONZE;

    @Column(precision = 19, scale = 2)
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;
}
