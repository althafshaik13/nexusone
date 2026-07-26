package dev.nexusone.ticket_service.dto;

import dev.nexusone.ticket_service.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTicketRequest(
        @NotBlank String subject,
        String description,
        @NotNull TicketPriority priority
) {
}
