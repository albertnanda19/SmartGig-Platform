package com.smartgig.user.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkillResponse {
    private Long id;
    private String name;
    private String slug;
    private String category;
    private String description;
    private String iconUrl;
    private Integer usageCount;
    private int distance;
}

