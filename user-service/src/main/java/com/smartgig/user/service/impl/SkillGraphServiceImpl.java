package com.smartgig.user.service.impl;

import com.smartgig.common.exception.ResourceNotFoundException;
import com.smartgig.user.dto.response.SkillGapAnalysisResponse;
import com.smartgig.user.dto.response.SkillGapAnalysisResponse.SkillGapItem;
import com.smartgig.user.dto.response.SkillResponse;
import com.smartgig.user.dto.response.SkillSimilarityResponse;
import com.smartgig.user.dto.response.SkillSimilarityResponse.SuggestedSkill;
import com.smartgig.user.entity.Skill;
import com.smartgig.user.entity.SkillRelation;
import com.smartgig.user.entity.SkillRelation.RelationType;
import com.smartgig.user.mapper.SkillMapper;
import com.smartgig.user.repository.SkillRelationRepository;
import com.smartgig.user.repository.SkillRepository;
import com.smartgig.user.repository.UserProfileRepository;
import com.smartgig.user.repository.UserSkillRepository;
import com.smartgig.user.service.SkillGraphService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillGraphServiceImpl implements SkillGraphService {
    private final UserSkillRepository userSkillRepository;
    private final SkillRelationRepository relationRepository;
    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;
    private final UserProfileRepository userProfileRepository;

    @Override
    public SkillSimilarityResponse calculateSkillSimilarity(Long userId1, Long userId2) {
        Set<Long> skills1 = new HashSet<>(userSkillRepository.findSkillIdsByUserId(userId1));
        Set<Long> skills2 = new HashSet<>(userSkillRepository.findSkillIdsByUserId(userId2));

        Set<Long> intersection = new HashSet<>(skills1);
        intersection.retainAll(skills2);

        Set<Long> union = new HashSet<>(skills1);
        union.addAll(skills2);

        double baseScore = union.isEmpty() ? 0.0 : (double) intersection.size() / (double) union.size();

        Set<Long> unique1 = new HashSet<>(skills1);
        unique1.removeAll(skills2);
        Set<Long> unique2 = new HashSet<>(skills2);
        unique2.removeAll(skills1);

        List<SkillRelation> relations = union.isEmpty() ? List.of() : relationRepository.findRelationsBySkillIds(new ArrayList<>(union));

        Map<Long, Skill> skillMap = skillRepository.findAllById(union).stream()
                .collect(Collectors.toMap(Skill::getId, s -> s));

        double boost = graphBoost(unique1, skills2, relations);
        double finalScore = Math.min(baseScore + boost, 1.0);

        List<String> commonNames = intersection.stream()
                .map(skillMap::get)
                .filter(s -> s != null)
                .map(Skill::getName)
                .sorted()
                .toList();

        List<String> uniqueNames1 = unique1.stream()
                .map(skillMap::get)
                .filter(s -> s != null)
                .map(Skill::getName)
                .sorted()
                .toList();

        List<String> uniqueNames2 = unique2.stream()
                .map(skillMap::get)
                .filter(s -> s != null)
                .map(Skill::getName)
                .sorted()
                .toList();

        List<SuggestedSkill> suggested = suggestedSkillsForUser1(unique1, skills2, relations, skillMap);

        return SkillSimilarityResponse.builder()
                .userId1(userId1)
                .userId2(userId2)
                .similarityScore(finalScore)
                .similarityLevel(levelOf(finalScore))
                .commonSkills(commonNames)
                .uniqueSkillsUser1(uniqueNames1)
                .uniqueSkillsUser2(uniqueNames2)
                .suggestedSkillsForUser1(suggested)
                .build();
    }

    @Override
    public SkillGapAnalysisResponse getSkillGapAnalysis(Long userId) {
        userProfileRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        Set<Long> userSkillIds = new HashSet<>(userSkillRepository.findSkillIdsByUserId(userId));
        List<SkillRelation> outgoing = userSkillIds.isEmpty()
                ? List.of()
                : relationRepository.findBySourceSkillIdIn(new ArrayList<>(userSkillIds));

        Map<Long, Skill> skillMap = new HashMap<>();
        if (!userSkillIds.isEmpty()) {
            skillRepository.findAllById(userSkillIds).forEach(s -> skillMap.put(s.getId(), s));
        }

        Set<Long> targetIds = outgoing.stream().map(r -> r.getTargetSkill().getId()).collect(Collectors.toSet());
        Set<Long> neededIds = new HashSet<>(targetIds);
        neededIds.removeAll(userSkillIds);
        if (!neededIds.isEmpty()) {
            skillRepository.findAllById(neededIds).forEach(s -> skillMap.put(s.getId(), s));
        }

        Map<Long, SkillGapItem> bestPerTarget = new LinkedHashMap<>();
        for (SkillRelation rel : outgoing) {
            Long targetId = rel.getTargetSkill().getId();
            if (userSkillIds.contains(targetId)) {
                continue;
            }
            Skill target = skillMap.get(targetId);
            Skill source = skillMap.get(rel.getSourceSkill().getId());
            if (target == null || source == null) {
                continue;
            }

            SkillGapItem item = SkillGapItem.builder()
                    .skillId(target.getId())
                    .skillName(target.getName())
                    .category(target.getCategory().name())
                    .relevanceScore(rel.getWeight().doubleValue())
                    .reason(reasonFor(rel.getRelationType(), source.getName(), target.getName()))
                    .relatedExistingSkill(source.getName())
                    .relationType(rel.getRelationType().name())
                    .build();

            SkillGapItem existing = bestPerTarget.get(targetId);
            if (existing == null || compareGapItem(item, existing) < 0) {
                bestPerTarget.put(targetId, item);
            }
        }

        List<SkillGapItem> suggestions = bestPerTarget.values().stream()
                .sorted(this::compareGapItem)
                .limit(10)
                .toList();

        int current = userSkillIds.size();
        return SkillGapAnalysisResponse.builder()
                .userId(userId)
                .suggestedSkills(suggestions)
                .currentSkillCount(current)
                .profileStrength(strengthOf(current))
                .build();
    }

    @Override
    @Cacheable(value = "skillRelations", key = "'related:' + #skillId + ':' + #depth")
    public List<SkillResponse> getRelatedSkills(Long skillId, int depth) {
        int maxDepth = Math.min(Math.max(depth, 1), 3);
        Skill source = skillRepository.findById(skillId).orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        Map<Long, Integer> distance = new HashMap<>();
        distance.put(source.getId(), 0);

        Deque<Long> queue = new ArrayDeque<>();
        queue.add(source.getId());

        int currentDepth = 0;
        while (!queue.isEmpty() && currentDepth < maxDepth) {
            int size = queue.size();
            List<Long> frontier = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                frontier.add(queue.poll());
            }

            List<SkillRelation> outgoing = relationRepository.findBySourceSkillIdIn(frontier);
            for (SkillRelation rel : outgoing) {
                Long next = rel.getTargetSkill().getId();
                if (!distance.containsKey(next)) {
                    distance.put(next, currentDepth + 1);
                    queue.add(next);
                }
            }
            currentDepth++;
        }

        Set<Long> ids = new HashSet<>(distance.keySet());
        ids.remove(source.getId());
        if (ids.isEmpty()) {
            return List.of();
        }

        Map<Long, Skill> skills = skillRepository.findAllById(ids).stream().collect(Collectors.toMap(Skill::getId, s -> s));
        return distance.entrySet().stream()
                .filter(e -> !e.getKey().equals(source.getId()))
                .map(e -> {
                    Skill s = skills.get(e.getKey());
                    if (s == null) {
                        return null;
                    }
                    SkillResponse resp = skillMapper.toResponse(s);
                    resp.setDistance(e.getValue());
                    return resp;
                })
                .filter(r -> r != null)
                .sorted(Comparator.comparingInt(SkillResponse::getDistance).thenComparing(SkillResponse::getName))
                .toList();
    }

    private double graphBoost(Set<Long> uniqueSkillsUser1, Set<Long> skillsUser2, List<SkillRelation> relations) {
        if (uniqueSkillsUser1.isEmpty() || skillsUser2.isEmpty() || relations.isEmpty()) {
            return 0.0;
        }

        double boost = 0.0;
        for (SkillRelation rel : relations) {
            Long src = rel.getSourceSkill().getId();
            Long tgt = rel.getTargetSkill().getId();
            if (uniqueSkillsUser1.contains(src) && skillsUser2.contains(tgt)) {
                boost += rel.getWeight().multiply(BigDecimal.valueOf(0.3)).doubleValue();
            }
        }
        return boost;
    }

    private List<SuggestedSkill> suggestedSkillsForUser1(Set<Long> uniqueSkillsUser1, Set<Long> skillsUser2, List<SkillRelation> relations, Map<Long, Skill> skillMap) {
        if (uniqueSkillsUser1.isEmpty() || skillsUser2.isEmpty() || relations.isEmpty()) {
            return List.of();
        }

        Map<Long, SuggestedSkill> best = new HashMap<>();
        for (SkillRelation rel : relations) {
            Long src = rel.getSourceSkill().getId();
            Long tgt = rel.getTargetSkill().getId();
            if (!uniqueSkillsUser1.contains(src) || !skillsUser2.contains(tgt)) {
                continue;
            }
            Skill target = skillMap.get(tgt);
            Skill source = skillMap.get(src);
            if (target == null || source == null) {
                continue;
            }

            double score = rel.getWeight().doubleValue();
            String reason = reasonFor(rel.getRelationType(), source.getName(), target.getName());
            SuggestedSkill candidate = SuggestedSkill.builder()
                    .skillId(target.getId())
                    .skillName(target.getName())
                    .relevanceScore(score)
                    .reason(reason)
                    .build();

            SuggestedSkill existing = best.get(target.getId());
            if (existing == null || Double.compare(candidate.getRelevanceScore(), existing.getRelevanceScore()) > 0) {
                best.put(target.getId(), candidate);
            }
        }

        return best.values().stream()
                .sorted(Comparator.comparingDouble(SuggestedSkill::getRelevanceScore).reversed().thenComparing(SuggestedSkill::getSkillName))
                .toList();
    }

    private String levelOf(double score) {
        if (score > 0.7) {
            return "HIGH";
        }
        if (score >= 0.4) {
            return "MEDIUM";
        }
        return "LOW";
    }

    private String strengthOf(int skillCount) {
        if (skillCount <= 2) {
            return "WEAK";
        }
        if (skillCount <= 5) {
            return "MODERATE";
        }
        if (skillCount <= 10) {
            return "STRONG";
        }
        return "EXPERT";
    }

    private int compareGapItem(SkillGapItem a, SkillGapItem b) {
        int w = Double.compare(b.getRelevanceScore(), a.getRelevanceScore());
        if (w != 0) {
            return w;
        }
        int p = Integer.compare(priorityOf(b.getRelationType()), priorityOf(a.getRelationType()));
        if (p != 0) {
            return p;
        }
        return a.getSkillName().compareToIgnoreCase(b.getSkillName());
    }

    private int priorityOf(String relationType) {
        RelationType rt = RelationType.valueOf(relationType);
        return switch (rt) {
            case REQUIRES -> 4;
            case ENABLES -> 3;
            case COMPLEMENTS -> 2;
            case LEADS_TO -> 1;
            case ALTERNATIVE_TO -> 0;
        };
    }

    private String reasonFor(RelationType type, String sourceName, String targetName) {
        return switch (type) {
            case REQUIRES -> "Required by " + sourceName;
            case ENABLES -> "Enables " + targetName;
            case COMPLEMENTS -> "Complements " + sourceName;
            case LEADS_TO -> "Leads to " + targetName;
            case ALTERNATIVE_TO -> "Alternative to " + targetName;
        };
    }
}

