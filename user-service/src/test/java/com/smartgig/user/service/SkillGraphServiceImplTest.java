package com.smartgig.user.service;

import com.smartgig.user.dto.response.SkillGapAnalysisResponse;
import com.smartgig.user.dto.response.SkillSimilarityResponse;
import com.smartgig.user.entity.Skill;
import com.smartgig.user.entity.SkillRelation;
import com.smartgig.user.entity.UserProfile;
import com.smartgig.user.mapper.SkillMapper;
import com.smartgig.user.repository.SkillRelationRepository;
import com.smartgig.user.repository.SkillRepository;
import com.smartgig.user.repository.UserProfileRepository;
import com.smartgig.user.repository.UserSkillRepository;
import com.smartgig.user.service.impl.SkillGraphServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillGraphServiceImplTest {

    @Mock
    UserSkillRepository userSkillRepository;

    @Mock
    SkillRelationRepository relationRepository;

    @Mock
    SkillRepository skillRepository;

    @Mock
    SkillMapper skillMapper;

    @Mock
    UserProfileRepository userProfileRepository;

    @InjectMocks
    SkillGraphServiceImpl service;

    @Test
    void whenUsersHaveIdenticalSkills_thenSimilarityIsOne() {
        when(userSkillRepository.findSkillIdsByUserId(1L)).thenReturn(List.of(10L, 11L));
        when(userSkillRepository.findSkillIdsByUserId(2L)).thenReturn(List.of(10L, 11L));
        when(relationRepository.findRelationsBySkillIds(List.of(10L, 11L))).thenReturn(List.of());

        Skill s10 = Skill.builder().id(10L).name("Java").build();
        Skill s11 = Skill.builder().id(11L).name("Spring Boot").build();
        when(skillRepository.findAllById(java.util.Set.of(10L, 11L))).thenReturn(List.of(s10, s11));

        SkillSimilarityResponse res = service.calculateSkillSimilarity(1L, 2L);
        assertThat(res.getSimilarityScore()).isEqualTo(1.0);
        assertThat(res.getSimilarityLevel()).isEqualTo("HIGH");
        assertThat(res.getCommonSkills()).containsExactlyInAnyOrder("Java", "Spring Boot");
    }

    @Test
    void whenUsersHaveNoCommonSkills_thenSimilarityIsZero() {
        when(userSkillRepository.findSkillIdsByUserId(1L)).thenReturn(List.of(10L));
        when(userSkillRepository.findSkillIdsByUserId(2L)).thenReturn(List.of(20L));
        when(relationRepository.findRelationsBySkillIds(anyList())).thenReturn(List.of());

        Skill s10 = Skill.builder().id(10L).name("Java").build();
        Skill s20 = Skill.builder().id(20L).name("Docker").build();
        when(skillRepository.findAllById(java.util.Set.of(10L, 20L))).thenReturn(List.of(s10, s20));

        SkillSimilarityResponse res = service.calculateSkillSimilarity(1L, 2L);
        assertThat(res.getSimilarityScore()).isEqualTo(0.0);
        assertThat(res.getSimilarityLevel()).isEqualTo("LOW");
    }

    @Test
    void whenPartialOverlap_thenSimilarityBetweenZeroAndOne() {
        when(userSkillRepository.findSkillIdsByUserId(1L)).thenReturn(List.of(10L, 11L));
        when(userSkillRepository.findSkillIdsByUserId(2L)).thenReturn(List.of(11L, 12L));
        when(relationRepository.findRelationsBySkillIds(anyList())).thenReturn(List.of());

        Skill s10 = Skill.builder().id(10L).name("Java").build();
        Skill s11 = Skill.builder().id(11L).name("Spring Boot").build();
        Skill s12 = Skill.builder().id(12L).name("Kafka").build();
        when(skillRepository.findAllById(java.util.Set.of(10L, 11L, 12L))).thenReturn(List.of(s10, s11, s12));

        SkillSimilarityResponse res = service.calculateSkillSimilarity(1L, 2L);
        assertThat(res.getSimilarityScore()).isGreaterThan(0.0);
        assertThat(res.getSimilarityScore()).isLessThan(1.0);
    }

    @Test
    void whenGraphBoostApplies_thenScoreHigherThanPureJaccard() {
        when(userSkillRepository.findSkillIdsByUserId(1L)).thenReturn(List.of(10L));
        when(userSkillRepository.findSkillIdsByUserId(2L)).thenReturn(List.of(11L));

        Skill javaSkill = Skill.builder().id(10L).name("Java").build();
        Skill spring = Skill.builder().id(11L).name("Spring Boot").build();

        SkillRelation rel = SkillRelation.builder()
                .id(1L)
                .sourceSkill(javaSkill)
                .targetSkill(spring)
                .relationType(SkillRelation.RelationType.ENABLES)
                .weight(new BigDecimal("0.80"))
                .build();

        when(relationRepository.findRelationsBySkillIds(List.of(10L, 11L))).thenReturn(List.of(rel));
        when(skillRepository.findAllById(java.util.Set.of(10L, 11L))).thenReturn(List.of(javaSkill, spring));

        SkillSimilarityResponse res = service.calculateSkillSimilarity(1L, 2L);
        assertThat(res.getSimilarityScore()).isGreaterThan(0.0);
    }

    @Test
    void getSkillGapAnalysis_doesNotSuggestOwnedSkills_andSortsByWeight() {
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(UserProfile.builder().userId(1L).build()));
        when(userSkillRepository.findSkillIdsByUserId(1L)).thenReturn(List.of(10L));

        Skill javaSkill = Skill.builder().id(10L).name("Java").category(Skill.SkillCategory.BACKEND).build();
        Skill spring = Skill.builder().id(11L).name("Spring Boot").category(Skill.SkillCategory.BACKEND).build();
        Skill docker = Skill.builder().id(12L).name("Docker").category(Skill.SkillCategory.DEVOPS).build();

        SkillRelation r1 = SkillRelation.builder()
                .sourceSkill(javaSkill)
                .targetSkill(spring)
                .relationType(SkillRelation.RelationType.ENABLES)
                .weight(new BigDecimal("0.70"))
                .build();
        SkillRelation r2 = SkillRelation.builder()
                .sourceSkill(javaSkill)
                .targetSkill(docker)
                .relationType(SkillRelation.RelationType.ENABLES)
                .weight(new BigDecimal("0.90"))
                .build();

        when(relationRepository.findBySourceSkillIdIn(List.of(10L))).thenReturn(List.of(r1, r2));
        when(skillRepository.findAllById(java.util.Set.of(10L))).thenReturn(List.of(javaSkill));
        when(skillRepository.findAllById(java.util.Set.of(11L, 12L))).thenReturn(List.of(spring, docker));

        SkillGapAnalysisResponse res = service.getSkillGapAnalysis(1L);
        assertThat(res.getSuggestedSkills()).extracting(SkillGapAnalysisResponse.SkillGapItem::getSkillId).doesNotContain(10L);
        assertThat(res.getSuggestedSkills()).isNotEmpty();
        assertThat(res.getSuggestedSkills().getFirst().getSkillId()).isEqualTo(12L);
    }
}

