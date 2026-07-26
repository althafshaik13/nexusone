package dev.nexusone.ticket_service.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignTicketRequest(
        @NotNull UUID agentId
) {
}
