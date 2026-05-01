package com.smartgig.auth.mapper;

import com.smartgig.auth.dto.request.RegisterRequest;
import com.smartgig.auth.entity.UserCredential;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-01T21:19:22+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Homebrew)"
)
@Component
public class UserCredentialMapperImpl implements UserCredentialMapper {

    @Override
    public UserCredential toEntity(RegisterRequest request) {
        if ( request == null ) {
            return null;
        }

        UserCredential.UserCredentialBuilder userCredential = UserCredential.builder();

        userCredential.username( request.getUsername() );
        userCredential.email( request.getEmail() );
        userCredential.role( request.getRole() );

        userCredential.active( true );
        userCredential.emailVerified( false );
        userCredential.failedLoginAttempts( 0 );

        return userCredential.build();
    }
}
