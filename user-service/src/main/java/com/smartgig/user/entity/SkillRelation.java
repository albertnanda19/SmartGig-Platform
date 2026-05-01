package com.smartgig.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "skill_relations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillRelation {

    public enum RelationType {
        REQUIRES,
        ENABLES,
        COMPLEMENTS,
        LEADS_TO,
        ALTERNATIVE_TO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_skill_id", nullable = false)
    private Skill sourceSkill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_skill_id", nullable = false)
    private Skill targetSkill;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 30)
    private RelationType relationType;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal weight;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}

