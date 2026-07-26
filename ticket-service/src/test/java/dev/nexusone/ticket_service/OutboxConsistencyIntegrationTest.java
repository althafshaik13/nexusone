package dev.nexusone.ticket_service;

import dev.nexusone.ticket_service.domain.OutboxEvent;
import dev.nexusone.ticket_service.domain.Ticket;
import dev.nexusone.ticket_service.domain.TicketPriority;
import dev.nexusone.ticket_service.domain.UserRole;
import dev.nexusone.ticket_service.dto.CreateTicketRequest;
import dev.nexusone.ticket_service.dto.CreateUserRequest;
import dev.nexusone.ticket_service.repository.OutboxEventRepository;
import dev.nexusone.ticket_service.repository.TicketRepository;
import dev.nexusone.ticket_service.service.TicketService;
import dev.nexusone.ticket_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class OutboxConsistencyIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    void rolledBackTransactionLeavesNoOutboxEvent() {
        UUID customerId = userService.createUser(
                new CreateUserRequest("outbox-rollback@nexusone.dev", UserRole.CUSTOMER)).getId();

        AtomicReference<UUID> ticketIdHolder = new AtomicReference<>();
        transactionTemplate.executeWithoutResult(status -> {
            Ticket ticket = ticketService.createTicket(
                    new CreateTicketRequest("Doomed ticket", null, TicketPriority.LOW), customerId);
            ticketIdHolder.set(ticket.getId());
            status.setRollbackOnly();
        });

        UUID ticketId = ticketIdHolder.get();
        assertNotNull(ticketId);
        assertFalse(ticketRepository.existsById(ticketId), "ticket row must be rolled back");
        assertTrue(outboxEventRepository.findByAggregateId(ticketId).isEmpty(),
                "no outbox event may survive a rolled-back domain transaction");
    }

    @Test
    void committedTicketGetsOutboxEventThatIsEventuallyPublished() throws InterruptedException {
        UUID customerId = userService.createUser(
                new CreateUserRequest("outbox-happy@nexusone.dev", UserRole.CUSTOMER)).getId();
        Ticket ticket = ticketService.createTicket(
                new CreateTicketRequest("Outbox happy path", null, TicketPriority.MEDIUM), customerId);

        List<OutboxEvent> events = outboxEventRepository.findByAggregateId(ticket.getId());
        assertEquals(1, events.size(), "exactly one outbox event staged for the created ticket");

        UUID outboxId = events.get(0).getId();
        long deadline = System.currentTimeMillis() + 15000;
        boolean published = false;
        while (System.currentTimeMillis() < deadline && !published) {
            published = outboxEventRepository.findById(outboxId)
                    .map(e -> e.getPublishedAt() != null)
                    .orElse(false);
            if (!published) {
                Thread.sleep(500);
            }
        }
        assertTrue(published, "outbox relay should publish and mark the event within 15s");
    }
}
