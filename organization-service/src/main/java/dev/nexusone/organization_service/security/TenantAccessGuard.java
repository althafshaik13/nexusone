package dev.nexusone.organization_service.security;

import dev.nexusone.organization_service.domain.Employee;
import dev.nexusone.organization_service.exception.TenantAccessDeniedException;
import dev.nexusone.organization_service.repository.EmployeeRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class TenantAccessGuard {

    private final EmployeeRepository employeeRepository;

    public TenantAccessGuard(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    public void requireAccess(Jwt jwt, UUID organizationId) {
        if (hasAdminRole(jwt)) {
            return;
        }
        UUID callerUserId = UUID.fromString(jwt.getSubject());
        Employee caller = employeeRepository.findByUserId(callerUserId).orElse(null);
        if (caller == null || !caller.getOrganizationId().equals(organizationId)) {
            throw new TenantAccessDeniedException();
        }
    }

    @SuppressWarnings("unchecked")
    private boolean hasAdminRole(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return false;
        }
        Object rolesObj = realmAccess.get("roles");
        return rolesObj instanceof List<?> roles && roles.contains("ADMIN");
    }
}
