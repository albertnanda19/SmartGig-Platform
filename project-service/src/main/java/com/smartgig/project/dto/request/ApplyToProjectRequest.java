package com.smartgig.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyToProjectRequest {
    @NotBlank
    private String coverLetter;

    @NotNull
    private BigDecimal proposedBudget;

    @NotNull
    private Integer estimatedDurationDays;

    @NotBlank
    private String freelancerUsername;
}

