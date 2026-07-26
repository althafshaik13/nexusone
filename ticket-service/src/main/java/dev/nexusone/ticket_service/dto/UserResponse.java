package dev.nexusone.ticket_service.dto;

import dev.nexusone.ticket_service.domain.AppUser;
import dev.nexusone.ticket_service.domain.UserRole;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        UserRole role,
        Instant createdAt
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
