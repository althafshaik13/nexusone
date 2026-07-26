package dev.nexusone.ticket_service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank String body,
        boolean internal
) {
}
