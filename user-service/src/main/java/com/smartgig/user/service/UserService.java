package com.smartgig.user.service;

import com.smartgig.user.dto.request.AddUserSkillRequest;
import com.smartgig.user.dto.request.CreateUserProfileRequest;
import com.smartgig.user.dto.request.UpdateUserProfileRequest;
import com.smartgig.user.dto.response.SkillGapAnalysisResponse;
import com.smartgig.user.dto.response.SkillSimilarityResponse;
import com.smartgig.user.dto.response.UserProfileResponse;
import com.smartgig.user.dto.response.UserSkillResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    UserProfileResponse createUserProfile(CreateUserProfileRequest request);

    UserProfileResponse getUserProfile(Long userId);

    UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request);

    UserSkillResponse addSkillToUser(Long userId, AddUserSkillRequest request);

    List<UserSkillResponse> getUserSkills(Long userId);

    void removeSkill(Long userId, Long skillId);

    Page<UserProfileResponse> getFreelancers(Pageable pageable);

    Page<UserProfileResponse> getFreelancersBySkill(Long skillId, Pageable pageable);

    SkillSimilarityResponse calculateSkillSimilarity(Long userId1, Long userId2);

    SkillGapAnalysisResponse getSkillGapAnalysis(Long userId);
}

