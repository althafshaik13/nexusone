package dev.nexusone.ticket_service.dto;

import dev.nexusone.ticket_service.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record TransitionTicketRequest(
        @NotNull TicketStatus targetStatus
) {
}
