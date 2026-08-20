package dev.nexusone.organization_service;

import dev.nexusone.organization_service.domain.Department;
import dev.nexusone.organization_service.domain.Employee;
import dev.nexusone.organization_service.domain.Organization;
import dev.nexusone.organization_service.domain.OrganizationEvent;
import dev.nexusone.organization_service.domain.Team;
import dev.nexusone.organization_service.dto.CreateDepartmentRequest;
import dev.nexusone.organization_service.dto.CreateEmployeeRequest;
import dev.nexusone.organization_service.dto.CreateOrganizationRequest;
import dev.nexusone.organization_service.dto.CreateTeamRequest;
import dev.nexusone.organization_service.service.OrganizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class OrganizationHierarchyIntegrationTest {

    @Autowired
    private OrganizationService organizationService;

    @Test
    @Transactional
    void buildsOrgHierarchyAndTraversesManagementChain() {
        UUID actorId = UUID.randomUUID();
        Organization org = organizationService.createOrganization(new CreateOrganizationRequest("Acme Corp"), actorId);
        Department engineering = organizationService.createDepartment(org.getId(), new CreateDepartmentRequest("Engineering"), actorId);
        Team platform = organizationService.createTeam(engineering.getId(), new CreateTeamRequest("Platform"), actorId);

        Employee cto = organizationService.createEmployee(org.getId(),
                new CreateEmployeeRequest(UUID.randomUUID(), "cto@acme.test", "CTO", engineering.getId(), null, null), actorId);
        Employee director = organizationService.createEmployee(org.getId(),
                new CreateEmployeeRequest(UUID.randomUUID(), "director@acme.test", "Director of Engineering",
                        engineering.getId(), platform.getId(), cto.getId()), actorId);
        Employee engineer = organizationService.createEmployee(org.getId(),
                new CreateEmployeeRequest(UUID.randomUUID(), "engineer@acme.test", "Software Engineer",
                        engineering.getId(), platform.getId(), director.getId()), actorId);

        List<Employee> chain = organizationService.getManagementChain(engineer.getId());
        assertEquals(2, chain.size());
        assertEquals(director.getId(), chain.get(0).getId());
        assertEquals(cto.getId(), chain.get(1).getId());

        List<Employee> ctoReports = organizationService.getDirectReports(cto.getId());
        assertEquals(1, ctoReports.size());
        assertEquals(director.getId(), ctoReports.get(0).getId());

        Employee foundByUserId = organizationService.getEmployeeByUserId(engineer.getUserId());
        assertEquals(engineer.getId(), foundByUserId.getId());

        List<Team> teams = organizationService.listTeams(engineering.getId());
        assertTrue(teams.stream().anyMatch(t -> t.getId().equals(platform.getId())));
    }

    @Test
    @Transactional
    void listEventsReturnsAuditTrailInChronologicalOrder() {
        UUID actorId = UUID.randomUUID();
        Organization org = organizationService.createOrganization(new CreateOrganizationRequest("Globex Corp"), actorId);
        Department engineering = organizationService.createDepartment(org.getId(), new CreateDepartmentRequest("Engineering"), actorId);
        Team platform = organizationService.createTeam(engineering.getId(), new CreateTeamRequest("Platform"), actorId);
        organizationService.createEmployee(org.getId(),
                new CreateEmployeeRequest(UUID.randomUUID(), "employee@globex.test", "Engineer", engineering.getId(), platform.getId(), null),
                actorId);

        List<OrganizationEvent> events = organizationService.listEvents(org.getId());

        assertEquals(4, events.size());
        assertEquals("ORGANIZATION_CREATED", events.get(0).getEventType());
        assertEquals("DEPARTMENT_CREATED", events.get(1).getEventType());
        assertEquals("TEAM_CREATED", events.get(2).getEventType());
        assertEquals("EMPLOYEE_CREATED", events.get(3).getEventType());
        for (int i = 1; i < events.size(); i++) {
            assertTrue(!events.get(i).getCreatedAt().isBefore(events.get(i - 1).getCreatedAt()));
        }
    }
}
