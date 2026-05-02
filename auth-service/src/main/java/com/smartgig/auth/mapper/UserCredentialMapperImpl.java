package com.smartgig.auth.mapper;

import com.smartgig.auth.dto.request.RegisterRequest;
import com.smartgig.auth.entity.UserCredential;
import org.springframework.stereotype.Component;

@Component
public class UserCredentialMapperImpl {

    public UserCredential toEntity(RegisterRequest request) {
        if (request == null) {
            return null;
        }

        UserCredential credential = new UserCredential();
        credential.setUsername(request.getUsername());
        credential.setEmail(request.getEmail());
        credential.setRole(request.getRole());
        credential.setActive(true);
        credential.setEmailVerified(false);
        credential.setFailedLoginAttempts(0);
        // passwordHash, id, userId, lockedUntil, lastLoginAt, createdAt, updatedAt
        // will be set separately

        return credential;
    }
}
