package dev.nexusone.ticket_service;

import dev.nexusone.ticket_service.domain.Ticket;
import dev.nexusone.ticket_service.domain.TicketComment;
import dev.nexusone.ticket_service.domain.TicketEvent;
import dev.nexusone.ticket_service.domain.TicketPriority;
import dev.nexusone.ticket_service.domain.TicketStatus;
import dev.nexusone.ticket_service.domain.UserRole;
import dev.nexusone.ticket_service.dto.CreateCommentRequest;
import dev.nexusone.ticket_service.dto.CreateTicketRequest;
import dev.nexusone.ticket_service.dto.CreateUserRequest;
import dev.nexusone.ticket_service.exception.InvalidTicketTransitionException;
import dev.nexusone.ticket_service.repository.TicketEventRepository;
import dev.nexusone.ticket_service.service.TicketService;
import dev.nexusone.ticket_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TicketServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private TicketEventRepository ticketEventRepository;

    @Test
    @Transactional
    void createAssignTransitionAndCommentOnTicketRecordsAuditTrail() {
        UUID customerId = userService.createUser(new CreateUserRequest("customer@nexusone.dev", UserRole.CUSTOMER)).getId();
        UUID agentId = userService.createUser(new CreateUserRequest("agent@nexusone.dev", UserRole.AGENT)).getId();

        Ticket ticket = ticketService.createTicket(
                new CreateTicketRequest("Cannot log in", "Getting a 500 on login", TicketPriority.HIGH), customerId);
        assertEquals(TicketStatus.CREATED, ticket.getStatus());

        Ticket assigned = ticketService.assignTicket(ticket.getId(), agentId, agentId);
        assertEquals(TicketStatus.ASSIGNED, assigned.getStatus());
        assertEquals(agentId, assigned.getAssignedAgentId());

        Ticket inProgress = ticketService.transitionStatus(ticket.getId(), TicketStatus.IN_PROGRESS, agentId);
        assertEquals(TicketStatus.IN_PROGRESS, inProgress.getStatus());

        assertThrows(InvalidTicketTransitionException.class,
                () -> ticketService.transitionStatus(ticket.getId(), TicketStatus.CREATED, agentId));

        TicketComment comment = ticketService.addComment(ticket.getId(),
                new CreateCommentRequest("Looking into it", false), agentId);
        assertEquals(ticket.getId(), comment.getTicketId());

        Ticket resolved = ticketService.transitionStatus(ticket.getId(), TicketStatus.RESOLVED, agentId);
        assertEquals(TicketStatus.RESOLVED, resolved.getStatus());

        List<TicketEvent> events = ticketEventRepository.findByTicketId(ticket.getId());
        assertTrue(events.size() >= 4);
    }
}
