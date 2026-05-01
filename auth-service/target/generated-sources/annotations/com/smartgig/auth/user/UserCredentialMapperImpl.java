package com.smartgig.auth.user;

import com.smartgig.auth.api.dto.UserCredentialResponse;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T20:30:51+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Homebrew)"
)
@Component
public class UserCredentialMapperImpl implements UserCredentialMapper {

    @Override
    public UserCredentialResponse toResponse(UserCredential entity) {
        if ( entity == null ) {
            return null;
        }

        UUID id = null;
        String email = null;
        Role role = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = entity.getId();
        email = entity.getEmail();
        role = entity.getRole();
        createdAt = entity.getCreatedAt();
        updatedAt = entity.getUpdatedAt();

        UserCredentialResponse userCredentialResponse = new UserCredentialResponse( id, email, role, createdAt, updatedAt );

        return userCredentialResponse;
    }
}
