package dev.nexusone.organization_service.security;

import dev.nexusone.organization_service.domain.Employee;
import dev.nexusone.organization_service.exception.TenantAccessDeniedException;
import dev.nexusone.organization_service.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class TenantAccessGuardTest {

    private final EmployeeRepository employeeRepository = Mockito.mock(EmployeeRepository.class);
    private final TenantAccessGuard guard = new TenantAccessGuard(employeeRepository);

    @Test
    void adminBypassesOrgCheckEvenWithNoEmployeeRecord() {
        Jwt admin = buildJwt(UUID.randomUUID().toString(), "ADMIN");
        assertDoesNotThrow(() -> guard.requireAccess(admin, UUID.randomUUID()));
    }

    @Test
    void memberOfTheOrganizationIsAllowed() {
        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        Employee employee = new Employee(orgId, userId, "member@acme.test", "Engineer", null, null, null);
        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.of(employee));

        Jwt jwt = buildJwt(userId.toString(), "CUSTOMER");
        assertDoesNotThrow(() -> guard.requireAccess(jwt, orgId));
    }

    @Test
    void nonMemberOfTheOrganizationIsDenied() {
        UUID userId = UUID.randomUUID();
        UUID employeeOrgId = UUID.randomUUID();
        UUID otherOrgId = UUID.randomUUID();
        Employee employee = new Employee(employeeOrgId, userId, "member@acme.test", "Engineer", null, null, null);
        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.of(employee));

        Jwt jwt = buildJwt(userId.toString(), "CUSTOMER");
        assertThrows(TenantAccessDeniedException.class, () -> guard.requireAccess(jwt, otherOrgId));
    }

    @Test
    void userWithNoEmployeeRecordIsDenied() {
        UUID userId = UUID.randomUUID();
        when(employeeRepository.findByUserId(userId)).thenReturn(Optional.empty());

        Jwt jwt = buildJwt(userId.toString(), "AGENT");
        assertThrows(TenantAccessDeniedException.class, () -> guard.requireAccess(jwt, UUID.randomUUID()));
    }

    private Jwt buildJwt(String subject, String... roles) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject(subject)
                .claim("realm_access", Map.of("roles", List.of(roles)))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }
}
