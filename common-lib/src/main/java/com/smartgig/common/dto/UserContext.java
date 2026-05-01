package com.smartgig.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserContext {
    private Long userId;
    private String email;
    private String role;
    private String username;
}

