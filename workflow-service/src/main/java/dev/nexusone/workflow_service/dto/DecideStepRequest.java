package dev.nexusone.workflow_service.dto;

import dev.nexusone.workflow_service.domain.Decision;
import jakarta.validation.constraints.NotNull;

public record DecideStepRequest(@NotNull Decision decision, String comment) {
}
