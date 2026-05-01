package com.smartgig.user.controller;

import com.smartgig.common.constants.AppConstants;
import com.smartgig.common.response.ApiResponse;
import com.smartgig.user.dto.request.AddUserSkillRequest;
import com.smartgig.user.dto.request.CreateUserProfileRequest;
import com.smartgig.user.dto.request.UpdateUserProfileRequest;
import com.smartgig.user.dto.response.SkillGapAnalysisResponse;
import com.smartgig.user.dto.response.SkillSimilarityResponse;
import com.smartgig.user.dto.response.UserProfileResponse;
import com.smartgig.user.dto.response.UserSkillResponse;
import com.smartgig.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User profile and skills")
public class UserController {
    private final UserService userService;

    @PostMapping("/profile")
    @Operation(summary = "Create my profile")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    public ResponseEntity<ApiResponse<UserProfileResponse>> createUserProfile(HttpServletRequest http, @Valid @RequestBody CreateUserProfileRequest request) {
        Long userId = extractUserIdFromHeader(http);
        request.setUserId(userId);
        UserProfileResponse response = userService.createUserProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created", response));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(HttpServletRequest http) {
        Long userId = extractUserIdFromHeader(http);
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(userId)));
    }

    @GetMapping("/{userId}/profile")
    @Operation(summary = "Get profile by userId")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(userId)));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update my profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(HttpServletRequest http, @RequestBody UpdateUserProfileRequest request) {
        Long userId = extractUserIdFromHeader(http);
        return ResponseEntity.ok(ApiResponse.success("Updated", userService.updateUserProfile(userId, request)));
    }

    @PostMapping("/skills")
    @Operation(summary = "Add skill to my profile")
    public ResponseEntity<ApiResponse<UserSkillResponse>> addSkill(HttpServletRequest http, @Valid @RequestBody AddUserSkillRequest request) {
        Long userId = extractUserIdFromHeader(http);
        return ResponseEntity.ok(ApiResponse.success("Added", userService.addSkillToUser(userId, request)));
    }

    @GetMapping("/skills")
    @Operation(summary = "Get my skills")
    public ResponseEntity<ApiResponse<List<UserSkillResponse>>> getMySkills(HttpServletRequest http) {
        Long userId = extractUserIdFromHeader(http);
        return ResponseEntity.ok(ApiResponse.success(userService.getUserSkills(userId)));
    }

    @GetMapping("/{userId}/skills")
    @Operation(summary = "Get skills by userId")
    public ResponseEntity<ApiResponse<List<UserSkillResponse>>> getUserSkills(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserSkills(userId)));
    }

    @DeleteMapping("/skills/{skillId}")
    @Operation(summary = "Remove my skill")
    public ResponseEntity<ApiResponse<Void>> removeSkill(HttpServletRequest http, @PathVariable Long skillId) {
        Long userId = extractUserIdFromHeader(http);
        userService.removeSkill(userId, skillId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/freelancers")
    @Operation(summary = "Get freelancers")
    public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> getFreelancers(
            @RequestParam(required = false) Long skillId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserProfileResponse> result = skillId == null ? userService.getFreelancers(pageable) : userService.getFreelancersBySkill(skillId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{userId1}/similarity/{userId2}")
    @Operation(summary = "Get skill similarity between two users")
    public ResponseEntity<ApiResponse<SkillSimilarityResponse>> getSkillSimilarity(@PathVariable Long userId1, @PathVariable Long userId2) {
        return ResponseEntity.ok(ApiResponse.success(userService.calculateSkillSimilarity(userId1, userId2)));
    }

    @GetMapping("/skill-gap")
    @Operation(summary = "Get my skill gap suggestions")
    public ResponseEntity<ApiResponse<SkillGapAnalysisResponse>> getMySkillGap(HttpServletRequest http) {
        Long userId = extractUserIdFromHeader(http);
        return ResponseEntity.ok(ApiResponse.success(userService.getSkillGapAnalysis(userId)));
    }

    private Long extractUserIdFromHeader(HttpServletRequest request) {
        String raw = request.getHeader(AppConstants.USER_ID_HEADER);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Long.parseLong(raw);
    }
}

