package com.smartgig.intelligence.client;

import com.smartgig.common.response.ApiResponse;
import com.smartgig.intelligence.config.FeignConfig;
import com.smartgig.intelligence.dto.response.UserProfileResponse;
import com.smartgig.intelligence.dto.response.UserSkillResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "user-service",
        configuration = FeignConfig.class,
        fallback = com.smartgig.intelligence.client.fallback.UserServiceClientFallback.class
)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}/profile")
    ApiResponse<UserProfileResponse> getUserProfile(@PathVariable Long userId);

    @GetMapping("/api/v1/users/{userId}/skills")
    ApiResponse<List<UserSkillResponse>> getUserSkills(@PathVariable Long userId);

    @GetMapping("/api/v1/users/freelancers")
    ApiResponse<SpringPageResponse<UserProfileResponse>> getFreelancers(
            @RequestParam(required = false) Long skillId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );
}

