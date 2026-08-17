package dev.nexusone.organization_service.web;

import dev.nexusone.organization_service.domain.Employee;
import dev.nexusone.organization_service.dto.EmployeeResponse;
import dev.nexusone.organization_service.security.TenantAccessGuard;
import dev.nexusone.organization_service.service.OrganizationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final OrganizationService organizationService;
    private final TenantAccessGuard tenantAccessGuard;

    public EmployeeController(OrganizationService organizationService, TenantAccessGuard tenantAccessGuard) {
        this.organizationService = organizationService;
        this.tenantAccessGuard = tenantAccessGuard;
    }

    @GetMapping("/me")
    public EmployeeResponse me(@AuthenticationPrincipal Jwt jwt) {
        return EmployeeResponse.from(organizationService.getEmployeeByUserId(UUID.fromString(jwt.getSubject())));
    }

    @GetMapping("/{id}")
    public EmployeeResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        Employee employee = organizationService.getEmployee(id);
        tenantAccessGuard.requireAccess(jwt, employee.getOrganizationId());
        return EmployeeResponse.from(employee);
    }

    @GetMapping("/{id}/management-chain")
    public List<EmployeeResponse> managementChain(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        Employee employee = organizationService.getEmployee(id);
        tenantAccessGuard.requireAccess(jwt, employee.getOrganizationId());
        return organizationService.getManagementChain(id).stream().map(EmployeeResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}/direct-reports")
    public List<EmployeeResponse> directReports(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        Employee employee = organizationService.getEmployee(id);
        tenantAccessGuard.requireAccess(jwt, employee.getOrganizationId());
        return organizationService.getDirectReports(id).stream().map(EmployeeResponse::from).collect(Collectors.toList());
    }
}
