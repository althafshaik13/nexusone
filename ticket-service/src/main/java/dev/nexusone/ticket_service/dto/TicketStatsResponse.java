package dev.nexusone.ticket_service.dto;

import java.util.Map;

public record TicketStatsResponse(
        Map<String, Long> countByStatus,
        Double averageResolutionHours
) {
}
