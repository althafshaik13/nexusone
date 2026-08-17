package dev.nexusone.organization_service.dto;

import dev.nexusone.organization_service.domain.Team;

import java.time.Instant;
import java.util.UUID;

public record TeamResponse(UUID id, UUID departmentId, String name, Instant createdAt) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(team.getId(), team.getDepartmentId(), team.getName(), team.getCreatedAt());
    }
}
