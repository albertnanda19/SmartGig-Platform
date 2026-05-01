package com.smartgig.project.entity;

import com.smartgig.project.statemachine.ProjectStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectStatus status;

    @Column(name = "budget_min", nullable = false, precision = 15, scale = 2)
    private BigDecimal budgetMin;

    @Column(name = "budget_max", nullable = false, precision = 15, scale = 2)
    private BigDecimal budgetMax;

    @Column(name = "budget_type", nullable = false, length = 20)
    private String budgetType;

    @Column
    private LocalDateTime deadline;

    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;

    @Column(name = "complexity_level", nullable = false, length = 20)
    private String complexityLevel;

    @Column(name = "max_applicants", nullable = false)
    private Integer maxApplicants;

    @Column(name = "current_applicant_count", nullable = false)
    private Integer currentApplicantCount;

    @Column(name = "selected_freelancer_id")
    private Long selectedFreelancerId;

    @Column(name = "views_count", nullable = false)
    private Integer viewsCount;

    @Column(name = "is_featured", nullable = false)
    private Boolean featured;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
}

