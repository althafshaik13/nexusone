package dev.nexusone.organization_service.web;

import dev.nexusone.organization_service.dto.CreateDepartmentRequest;
import dev.nexusone.organization_service.dto.CreateEmployeeRequest;
import dev.nexusone.organization_service.dto.CreateOrganizationRequest;
import dev.nexusone.organization_service.dto.DepartmentResponse;
import dev.nexusone.organization_service.dto.EmployeeResponse;
import dev.nexusone.organization_service.dto.OrganizationResponse;
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
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final TenantAccessGuard tenantAccessGuard;

    public OrganizationController(OrganizationService organizationService, TenantAccessGuard tenantAccessGuard) {
        this.organizationService = organizationService;
        this.tenantAccessGuard = tenantAccessGuard;
    }

    @PostMapping
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationResponse response = OrganizationResponse.from(organizationService.createOrganization(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<OrganizationResponse> list() {
        return organizationService.listOrganizations().stream().map(OrganizationResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public OrganizationResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        tenantAccessGuard.requireAccess(jwt, id);
        return OrganizationResponse.from(organizationService.getOrganization(id));
    }

    @PostMapping("/{id}/departments")
    public ResponseEntity<DepartmentResponse> createDepartment(@PathVariable UUID id,
                                                                 @Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse response = DepartmentResponse.from(organizationService.createDepartment(id, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/departments")
    public List<DepartmentResponse> listDepartments(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        tenantAccessGuard.requireAccess(jwt, id);
        return organizationService.listDepartments(id).stream().map(DepartmentResponse::from).collect(Collectors.toList());
    }

    @PostMapping("/{id}/employees")
    public ResponseEntity<EmployeeResponse> createEmployee(@PathVariable UUID id,
                                                             @Valid @RequestBody CreateEmployeeRequest request) {
        EmployeeResponse response = EmployeeResponse.from(organizationService.createEmployee(id, request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/employees")
    public List<EmployeeResponse> listEmployees(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        tenantAccessGuard.requireAccess(jwt, id);
        return organizationService.listEmployees(id).stream().map(EmployeeResponse::from).collect(Collectors.toList());
    }
}
