package dev.nexusone.organization_service.repository;

import dev.nexusone.organization_service.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    List<Organization> findAllByOrderByNameAsc();
}
