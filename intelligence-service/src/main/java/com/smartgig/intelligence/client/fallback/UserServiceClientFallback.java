package com.smartgig.intelligence.client.fallback;

import com.smartgig.common.response.ApiResponse;
import com.smartgig.intelligence.client.UserServiceClient;
import com.smartgig.intelligence.client.SpringPageResponse;
import com.smartgig.intelligence.dto.response.UserProfileResponse;
import com.smartgig.intelligence.dto.response.UserSkillResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserServiceClientFallback implements UserServiceClient {
    @Override
    public ApiResponse<UserProfileResponse> getUserProfile(Long userId) {
        return ApiResponse.error("User service unavailable");
    }

    @Override
    public ApiResponse<List<UserSkillResponse>> getUserSkills(Long userId) {
        return ApiResponse.error("User service unavailable");
    }

    @Override
    public ApiResponse<SpringPageResponse<UserProfileResponse>> getFreelancers(Long skillId, int page, int size) {
        return ApiResponse.error("User service unavailable");
    }
}

