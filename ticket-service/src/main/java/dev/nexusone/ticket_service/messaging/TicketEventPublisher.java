package dev.nexusone.ticket_service.messaging;

import dev.nexusone.ticket_service.domain.OutboxEvent;
import dev.nexusone.ticket_service.domain.Ticket;
import dev.nexusone.ticket_service.domain.TicketStatus;
import dev.nexusone.ticket_service.repository.OutboxEventRepository;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

@Component
public class TicketEventPublisher {

    public static final String TOPIC_TICKET_CREATED = "ticket.created";
    public static final String TOPIC_TICKET_STATUS_CHANGED = "ticket.status.changed";
    public static final String AGGREGATE_TICKET = "TICKET";

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public TicketEventPublisher(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void publishTicketCreated(Ticket ticket) {
        TicketDomainEvent event = new TicketDomainEvent(
                "TICKET_CREATED",
                ticket.getId(),
                ticket.getCustomerId(),
                ticket.getAssignedAgentId(),
                ticket.getStatus().name(),
                null,
                ticket.getPriority().name(),
                Instant.now());
        stage(TOPIC_TICKET_CREATED, event);
    }

    public void publishStatusChanged(Ticket ticket, TicketStatus previousStatus) {
        TicketDomainEvent event = new TicketDomainEvent(
                "TICKET_STATUS_CHANGED",
                ticket.getId(),
                ticket.getCustomerId(),
                ticket.getAssignedAgentId(),
                ticket.getStatus().name(),
                previousStatus.name(),
                ticket.getPriority().name(),
                Instant.now());
        stage(TOPIC_TICKET_STATUS_CHANGED, event);
    }

    private void stage(String topic, TicketDomainEvent event) {
        // Transactional outbox: the event row commits (or rolls back) atomically
        // with the domain change that produced it. The OutboxRelay delivers it to
        // Kafka afterwards, giving at-least-once delivery with no dual-write gap.
        String payload = objectMapper.writeValueAsString(event);
        outboxEventRepository.save(new OutboxEvent(
                AGGREGATE_TICKET, event.ticketId(), topic, event.ticketId().toString(), payload));
    }
}
