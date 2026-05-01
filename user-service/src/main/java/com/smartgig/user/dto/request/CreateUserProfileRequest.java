package com.smartgig.user.dto.request;

import com.smartgig.user.entity.UserProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserProfileRequest {
    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(max = 100)
    private String fullName;

    @NotNull
    private UserProfile.UserRole role;

    private String bio;
    private String avatarUrl;
}

