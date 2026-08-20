package dev.nexusone.organization_service.repository;

import dev.nexusone.organization_service.domain.OrganizationEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrganizationEventRepository extends JpaRepository<OrganizationEvent, UUID> {
    List<OrganizationEvent> findByOrganizationIdOrderByCreatedAtAsc(UUID organizationId);
}
