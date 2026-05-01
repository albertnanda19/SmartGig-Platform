package com.smartgig.user.service.impl;

import com.smartgig.common.constants.KafkaTopics;
import com.smartgig.common.exception.BusinessException;
import com.smartgig.common.exception.ResourceNotFoundException;
import com.smartgig.user.dto.request.AddUserSkillRequest;
import com.smartgig.user.dto.request.CreateUserProfileRequest;
import com.smartgig.user.dto.request.UpdateUserProfileRequest;
import com.smartgig.user.dto.response.SkillGapAnalysisResponse;
import com.smartgig.user.dto.response.SkillSimilarityResponse;
import com.smartgig.user.dto.response.UserProfileResponse;
import com.smartgig.user.dto.response.UserSkillResponse;
import com.smartgig.user.entity.Skill;
import com.smartgig.user.entity.UserProfile;
import com.smartgig.user.entity.UserSkill;
import com.smartgig.user.event.UserRegisteredEvent;
import com.smartgig.user.mapper.SkillMapper;
import com.smartgig.user.mapper.UserProfileMapper;
import com.smartgig.user.repository.SkillRepository;
import com.smartgig.user.repository.UserProfileRepository;
import com.smartgig.user.repository.UserSkillRepository;
import com.smartgig.user.service.SkillGraphService;
import com.smartgig.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserProfileRepository userProfileRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserProfileMapper userProfileMapper;
    private final SkillMapper skillMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final CacheManager cacheManager;
    private final SkillGraphService skillGraphService;

    @Override
    @Transactional
    public UserProfileResponse createUserProfile(CreateUserProfileRequest request) {
        if (userProfileRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new BusinessException("Profile already exists");
        }

        UserProfile saved = userProfileRepository.save(userProfileMapper.toEntity(request));
        UserProfileResponse response = userProfileMapper.toResponse(saved);
        putUserProfileCache(saved.getUserId(), response);

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(saved.getUserId())
                .email(saved.getEmail())
                .username(saved.getUsername())
                .role(saved.getRole().name())
                .registeredAt(LocalDateTime.now(ZoneOffset.UTC))
                .eventId(UUID.randomUUID().toString())
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                kafkaTemplate.send(KafkaTopics.USER_REGISTERED, String.valueOf(saved.getUserId()), event);
            }
        });

        return response;
    }

    @Override
    @Cacheable(value = "userProfiles", key = "#userId")
    public UserProfileResponse getUserProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return userProfileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userProfiles", key = "#userId")
    public UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        if (request.getFullName() != null) profile.setFullName(request.getFullName());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());
        if (request.getHourlyRate() != null) profile.setHourlyRate(request.getHourlyRate());
        if (request.getYearsOfExperience() != null) profile.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getPortfolioUrl() != null) profile.setPortfolioUrl(request.getPortfolioUrl());
        if (request.getLinkedinUrl() != null) profile.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl() != null) profile.setGithubUrl(request.getGithubUrl());
        if (request.getAvailable() != null) profile.setAvailable(request.getAvailable());

        UserProfile saved = userProfileRepository.save(profile);
        UserProfileResponse response = userProfileMapper.toResponse(saved);
        putUserProfileCache(userId, response);
        return response;
    }

    @Override
    @Transactional
    public UserSkillResponse addSkillToUser(Long userId, AddUserSkillRequest request) {
        userProfileRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        Skill skill = skillRepository.findById(request.getSkillId()).orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        if (userSkillRepository.findByUserIdAndSkillId(userId, skill.getId()).isPresent()) {
            throw new BusinessException("Skill already added");
        }

        UserSkill.ProficiencyLevel level = request.getProficiencyLevel() == null ? UserSkill.ProficiencyLevel.INTERMEDIATE : request.getProficiencyLevel();
        Integer yoe = request.getYearsOfExperience() == null ? 0 : request.getYearsOfExperience();
        Boolean primary = request.getPrimary() != null && request.getPrimary();

        UserSkill saved = userSkillRepository.save(UserSkill.builder()
                .userId(userId)
                .skill(skill)
                .proficiencyLevel(level)
                .yearsOfExperience(yoe)
                .primary(primary)
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build());

        skill.setUsageCount((skill.getUsageCount() == null ? 0 : skill.getUsageCount()) + 1);
        skillRepository.save(skill);

        evictUserCaches(userId);

        return UserSkillResponse.builder()
                .userId(userId)
                .skill(skillMapper.toResponse(skill))
                .proficiencyLevel(saved.getProficiencyLevel().name())
                .yearsOfExperience(saved.getYearsOfExperience())
                .primary(saved.getPrimary())
                .build();
    }

    @Override
    public List<UserSkillResponse> getUserSkills(Long userId) {
        List<UserSkill> list = userSkillRepository.findByUserId(userId);
        return list.stream()
                .map(us -> UserSkillResponse.builder()
                        .userId(us.getUserId())
                        .skill(skillMapper.toResponse(us.getSkill()))
                        .proficiencyLevel(us.getProficiencyLevel().name())
                        .yearsOfExperience(us.getYearsOfExperience())
                        .primary(us.getPrimary())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void removeSkill(Long userId, Long skillId) {
        UserSkill us = userSkillRepository.findByUserIdAndSkillId(userId, skillId)
                .orElseThrow(() -> new ResourceNotFoundException("User skill not found"));
        userSkillRepository.delete(us);
        evictUserCaches(userId);
    }

    @Override
    public Page<UserProfileResponse> getFreelancers(Pageable pageable) {
        return userProfileRepository.findByRoleAndAvailableTrue(UserProfile.UserRole.FREELANCER, pageable).map(userProfileMapper::toResponse);
    }

    @Override
    @Cacheable(value = "freelancersBySkill", key = "'skill:' + #skillId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<UserProfileResponse> getFreelancersBySkill(Long skillId, Pageable pageable) {
        List<UserSkill> userSkills = userSkillRepository.findBySkillId(skillId);
        Set<Long> userIds = userSkills.stream().map(UserSkill::getUserId).collect(Collectors.toSet());
        if (userIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<UserProfile> page = userProfileRepository.findByUserIdInAndRoleAndAvailableTrue(userIds, UserProfile.UserRole.FREELANCER, pageable);
        return page.map(userProfileMapper::toResponse);
    }

    @Override
    public SkillSimilarityResponse calculateSkillSimilarity(Long userId1, Long userId2) {
        return skillGraphService.calculateSkillSimilarity(userId1, userId2);
    }

    @Override
    public SkillGapAnalysisResponse getSkillGapAnalysis(Long userId) {
        return skillGraphService.getSkillGapAnalysis(userId);
    }

    private void putUserProfileCache(Long userId, UserProfileResponse response) {
        Cache cache = cacheManager.getCache("userProfiles");
        if (cache != null) {
            cache.put(userId, response);
        }
    }

    private void evictUserCaches(Long userId) {
        Cache cache = cacheManager.getCache("userProfiles");
        if (cache != null) {
            cache.evict(userId);
        }
        Cache freelancers = cacheManager.getCache("freelancersBySkill");
        if (freelancers != null) {
            freelancers.clear();
        }
    }
}

