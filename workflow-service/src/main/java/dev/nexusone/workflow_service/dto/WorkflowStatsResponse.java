package dev.nexusone.workflow_service.dto;

import java.util.Map;

public record WorkflowStatsResponse(
        Map<String, Long> countByStatus,
        Map<String, Long> countByRequestType,
        Double averageDecisionHours
) {
}
