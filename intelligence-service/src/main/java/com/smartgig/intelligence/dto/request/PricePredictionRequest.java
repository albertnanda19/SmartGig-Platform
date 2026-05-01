package com.smartgig.intelligence.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PricePredictionRequest {
    @NotBlank
    private String category;

    @NotBlank
    private String complexityLevel;

    private LocalDateTime deadline;

    @Min(1)
    private int estimatedDurationDays;

    @NotEmpty
    private List<String> requiredSkills;

    private Double clientBudgetMin;
    private Double clientBudgetMax;
}

