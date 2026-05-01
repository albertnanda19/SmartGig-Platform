package com.smartgig.user.controller;

import com.smartgig.common.response.ApiResponse;
import com.smartgig.user.dto.response.SkillResponse;
import com.smartgig.user.entity.Skill.SkillCategory;
import com.smartgig.user.service.SkillGraphService;
import com.smartgig.user.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Skills", description = "Skills and graph")
public class SkillController {
    private final SkillService skillService;
    private final SkillGraphService skillGraphService;

    @GetMapping
    @Operation(summary = "List skills")
    public ResponseEntity<ApiResponse<Page<SkillResponse>>> listSkills(
            @RequestParam(required = false) SkillCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(skillService.getSkills(category, pageable)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get skill by id")
    public ResponseEntity<ApiResponse<SkillResponse>> getSkill(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(skillService.getSkill(id)));
    }

    @GetMapping("/{id}/related")
    @Operation(summary = "Get related skills by BFS traversal")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> relatedSkills(
            @PathVariable Long id,
            @RequestParam(defaultValue = "2") @Min(1) @Max(3) int depth
    ) {
        return ResponseEntity.ok(ApiResponse.success(skillGraphService.getRelatedSkills(id, depth)));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending skills by usage count")
    public ResponseEntity<ApiResponse<List<SkillResponse>>> trending(@RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(ApiResponse.success(skillService.getTrendingSkills(limit)));
    }
}

