package com.smartgig.user.repository;

import com.smartgig.user.entity.UserProfile;
import com.smartgig.user.entity.UserProfile.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);

    Optional<UserProfile> findByEmail(String email);

    Page<UserProfile> findByRoleAndAvailableTrue(UserRole role, Pageable pageable);

    Page<UserProfile> findByUserIdInAndRoleAndAvailableTrue(Collection<Long> userIds, UserRole role, Pageable pageable);

    @Query("SELECT u FROM UserProfile u WHERE u.role = 'FREELANCER' AND u.averageRating >= :minRating ORDER BY u.averageRating DESC")
    Page<UserProfile> findTopFreelancersByRating(@Param("minRating") BigDecimal minRating, Pageable pageable);
}

