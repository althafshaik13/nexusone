package dev.nexusone.organization_service.repository;

import dev.nexusone.organization_service.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Optional<Employee> findByUserId(UUID userId);
    List<Employee> findByManagerId(UUID managerId);
}
