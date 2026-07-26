package dev.nexusone.ticket_service.dto;

import dev.nexusone.ticket_service.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @Email @NotNull String email,
        @NotNull UserRole role
) {
}
