package dev.nexusone.ticket_service.repository;

import dev.nexusone.ticket_service.domain.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop50ByPublishedAtIsNullOrderByCreatedAtAsc();
    List<OutboxEvent> findByAggregateId(UUID aggregateId);
}
