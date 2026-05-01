package com.smartgig.project.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ProjectSearchRequest {
    private String keyword;
    private String category;
    private List<String> skills;
    private Double minBudget;
    private Double maxBudget;
    private String complexityLevel;
    private Boolean onlyActive;
    private String sortBy;
    private int page = 0;
    private int size = 10;
}

