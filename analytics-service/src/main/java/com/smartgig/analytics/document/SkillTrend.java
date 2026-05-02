package com.smartgig.analytics.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "skill_trends")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillTrend {
    @Id
    private String id;

    @Indexed
    private Long skillId;

    @Indexed
    private String skillName;

    private String skillCategory;

    @Indexed
    private String weekLabel;

    private int demandCount;
    private int applicationCount;
    private double avgProjectBudget;
    private double growthRate;
    private int rank;

    @CreatedDate
    private LocalDateTime calculatedAt;
}

