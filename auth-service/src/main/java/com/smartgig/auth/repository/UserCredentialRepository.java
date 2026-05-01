package com.smartgig.auth.repository;

import com.smartgig.auth.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {
    Optional<UserCredential> findByEmail(String email);

    Optional<UserCredential> findByUserId(Long userId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}

