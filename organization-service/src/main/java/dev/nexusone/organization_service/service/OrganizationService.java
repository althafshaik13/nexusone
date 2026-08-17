package dev.nexusone.organization_service.service;

import dev.nexusone.organization_service.domain.Department;
import dev.nexusone.organization_service.domain.Employee;
import dev.nexusone.organization_service.domain.Organization;
import dev.nexusone.organization_service.domain.Team;
import dev.nexusone.organization_service.dto.CreateDepartmentRequest;
import dev.nexusone.organization_service.dto.CreateEmployeeRequest;
import dev.nexusone.organization_service.dto.CreateOrganizationRequest;
import dev.nexusone.organization_service.dto.CreateTeamRequest;
import dev.nexusone.organization_service.exception.DepartmentNotFoundException;
import dev.nexusone.organization_service.exception.EmployeeNotFoundException;
import dev.nexusone.organization_service.exception.OrganizationNotFoundException;
import dev.nexusone.organization_service.repository.DepartmentRepository;
import dev.nexusone.organization_service.repository.EmployeeRepository;
import dev.nexusone.organization_service.repository.OrganizationRepository;
import dev.nexusone.organization_service.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrganizationService {

    private static final int MAX_CHAIN_DEPTH = 50;

    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final EmployeeRepository employeeRepository;

    public OrganizationService(OrganizationRepository organizationRepository,
                                DepartmentRepository departmentRepository,
                                TeamRepository teamRepository,
                                EmployeeRepository employeeRepository) {
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.teamRepository = teamRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public Organization createOrganization(CreateOrganizationRequest request) {
        return organizationRepository.save(new Organization(request.name()));
    }

    @Transactional(readOnly = true)
    public Organization getOrganization(UUID id) {
        return organizationRepository.findById(id).orElseThrow(() -> new OrganizationNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Organization> listOrganizations() {
        return organizationRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Department createDepartment(UUID organizationId, CreateDepartmentRequest request) {
        getOrganization(organizationId);
        return departmentRepository.save(new Department(organizationId, request.name()));
    }

    @Transactional(readOnly = true)
    public List<Department> listDepartments(UUID organizationId) {
        getOrganization(organizationId);
        return departmentRepository.findByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public UUID resolveOrganizationIdForDepartment(UUID departmentId) {
        return departmentRepository.findById(departmentId)
                .map(Department::getOrganizationId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId));
    }

    @Transactional
    public Team createTeam(UUID departmentId, CreateTeamRequest request) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new DepartmentNotFoundException(departmentId);
        }
        return teamRepository.save(new Team(departmentId, request.name()));
    }

    @Transactional(readOnly = true)
    public List<Team> listTeams(UUID departmentId) {
        if (!departmentRepository.existsById(departmentId)) {
            throw new DepartmentNotFoundException(departmentId);
        }
        return teamRepository.findByDepartmentId(departmentId);
    }

    @Transactional
    public Employee createEmployee(UUID organizationId, CreateEmployeeRequest request) {
        getOrganization(organizationId);
        Employee employee = new Employee(organizationId, request.userId(), request.email(), request.jobTitle(),
                request.departmentId(), request.teamId(), request.managerId());
        return employeeRepository.save(employee);
    }

    @Transactional(readOnly = true)
    public List<Employee> listEmployees(UUID organizationId) {
        getOrganization(organizationId);
        return employeeRepository.findByOrganizationId(organizationId);
    }

    @Transactional(readOnly = true)
    public Employee getEmployee(UUID id) {
        return employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeByUserId(UUID userId) {
        return employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new EmployeeNotFoundException("No employee record linked to user " + userId));
    }

    @Transactional(readOnly = true)
    public List<Employee> getDirectReports(UUID managerId) {
        getEmployee(managerId);
        return employeeRepository.findByManagerId(managerId);
    }

    @Transactional(readOnly = true)
    public List<Employee> getManagementChain(UUID employeeId) {
        List<Employee> chain = new ArrayList<>();
        Employee current = getEmployee(employeeId);
        int depth = 0;
        while (current.getManagerId() != null && depth < MAX_CHAIN_DEPTH) {
            Employee manager = getEmployee(current.getManagerId());
            chain.add(manager);
            current = manager;
            depth++;
        }
        return chain;
    }
}
