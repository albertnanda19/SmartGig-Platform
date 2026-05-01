package com.smartgig.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "project_applications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "freelancer_id", nullable = false)
    private Long freelancerId;

    @Column(name = "freelancer_username", nullable = false, length = 50)
    private String freelancerUsername;

    @Column(name = "cover_letter", nullable = false, columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "proposed_budget", nullable = false, precision = 15, scale = 2)
    private BigDecimal proposedBudget;

    @Column(name = "estimated_duration_days", nullable = false)
    private Integer estimatedDurationDays;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "matching_score", precision = 5, scale = 4)
    private BigDecimal matchingScore;

    @Column(name = "client_notes", columnDefinition = "TEXT")
    private String clientNotes;

    @Column(name = "applied_at", nullable = false)
    private LocalDateTime appliedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
}

