package dev.nexusone.organization_service.web;

import dev.nexusone.organization_service.dto.CreateTeamRequest;
import dev.nexusone.organization_service.dto.TeamResponse;
import dev.nexusone.organization_service.security.TenantAccessGuard;
import dev.nexusone.organization_service.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final OrganizationService organizationService;
    private final TenantAccessGuard tenantAccessGuard;

    public DepartmentController(OrganizationService organizationService, TenantAccessGuard tenantAccessGuard) {
        this.organizationService = organizationService;
        this.tenantAccessGuard = tenantAccessGuard;
    }

    @PostMapping("/{id}/teams")
    public ResponseEntity<TeamResponse> createTeam(@AuthenticationPrincipal Jwt jwt,
                                                     @PathVariable UUID id,
                                                     @Valid @RequestBody CreateTeamRequest request) {
        UUID actorId = UUID.fromString(jwt.getSubject());
        TeamResponse response = TeamResponse.from(organizationService.createTeam(id, request, actorId));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/teams")
    public List<TeamResponse> listTeams(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        UUID organizationId = organizationService.resolveOrganizationIdForDepartment(id);
        tenantAccessGuard.requireAccess(jwt, organizationId);
        return organizationService.listTeams(id).stream().map(TeamResponse::from).collect(Collectors.toList());
    }
}
