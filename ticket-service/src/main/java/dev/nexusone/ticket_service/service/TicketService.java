package dev.nexusone.ticket_service.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import dev.nexusone.ticket_service.domain.Ticket;
import dev.nexusone.ticket_service.domain.TicketComment;
import dev.nexusone.ticket_service.domain.TicketEvent;
import dev.nexusone.ticket_service.domain.TicketStateMachine;
import dev.nexusone.ticket_service.domain.TicketStatus;
import dev.nexusone.ticket_service.dto.CreateCommentRequest;
import dev.nexusone.ticket_service.dto.CreateTicketRequest;
import dev.nexusone.ticket_service.exception.InvalidTicketTransitionException;
import dev.nexusone.ticket_service.exception.TicketNotFoundException;
import dev.nexusone.ticket_service.exception.UserNotFoundException;
import dev.nexusone.ticket_service.messaging.TicketEventPublisher;
import dev.nexusone.ticket_service.repository.TicketCommentRepository;
import dev.nexusone.ticket_service.repository.TicketEventRepository;
import dev.nexusone.ticket_service.repository.TicketRepository;
import dev.nexusone.ticket_service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketEventRepository ticketEventRepository;
    private final TicketCommentRepository ticketCommentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final TicketEventPublisher eventPublisher;

    public TicketService(TicketRepository ticketRepository,
                          TicketEventRepository ticketEventRepository,
                          TicketCommentRepository ticketCommentRepository,
                          UserRepository userRepository,
                          ObjectMapper objectMapper,
                          TicketEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.ticketEventRepository = ticketEventRepository;
        this.ticketCommentRepository = ticketCommentRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Ticket createTicket(CreateTicketRequest request, UUID customerId) {
        if (!userRepository.existsById(customerId)) {
            throw new UserNotFoundException(customerId);
        }
        Ticket ticket = new Ticket(request.subject(), request.description(), customerId, request.priority());
        ticket = ticketRepository.save(ticket);
        recordEvent(ticket.getId(), "TICKET_CREATED", customerId, Map.of("status", ticket.getStatus().name()));
        eventPublisher.publishTicketCreated(ticket);
        return ticket;
    }

    @Transactional(readOnly = true)
    public Ticket getTicket(UUID ticketId) {
        return ticketRepository.findById(ticketId).orElseThrow(() -> new TicketNotFoundException(ticketId));
    }

    @Transactional(readOnly = true)
    public List<Ticket> listTickets(TicketStatus statusFilter, UUID customerIdFilter, String q) {
        return ticketRepository.search(statusFilter, customerIdFilter, q == null ? "" : q);
    }

    @Transactional
    public Ticket assignTicket(UUID ticketId, UUID agentId, UUID actorId) {
        if (!userRepository.existsById(agentId)) {
            throw new UserNotFoundException(agentId);
        }
        Ticket ticket = getTicket(ticketId);
        TicketStatus target = TicketStatus.ASSIGNED;
        TicketStatus previous = ticket.getStatus();
        if (!TicketStateMachine.canTransition(previous, target)) {
            throw new InvalidTicketTransitionException(previous, target);
        }
        ticket.setAssignedAgentId(agentId);
        ticket.setStatus(target);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("from", previous.name());
        payload.put("to", target.name());
        payload.put("agentId", agentId.toString());
        recordEvent(ticketId, "TICKET_ASSIGNED", actorId, payload);
        eventPublisher.publishStatusChanged(ticket, previous);
        return ticket;
    }

    @Transactional
    public Ticket transitionStatus(UUID ticketId, TicketStatus target, UUID actorId) {
        Ticket ticket = getTicket(ticketId);
        TicketStatus previous = ticket.getStatus();
        if (!TicketStateMachine.canTransition(previous, target)) {
            throw new InvalidTicketTransitionException(previous, target);
        }
        ticket.setStatus(target);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("from", previous.name());
        payload.put("to", target.name());
        recordEvent(ticketId, "TICKET_STATUS_CHANGED", actorId, payload);
        eventPublisher.publishStatusChanged(ticket, previous);
        return ticket;
    }

    @Transactional
    public TicketComment addComment(UUID ticketId, CreateCommentRequest request, UUID authorId) {
        getTicket(ticketId);
        TicketComment comment = new TicketComment(ticketId, authorId, request.body(), request.internal());
        comment = ticketCommentRepository.save(comment);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("commentId", comment.getId().toString());
        payload.put("internal", request.internal());
        recordEvent(ticketId, "COMMENT_ADDED", authorId, payload);
        return comment;
    }

    @Transactional(readOnly = true)
    public List<TicketComment> listComments(UUID ticketId) {
        getTicket(ticketId);
        return ticketCommentRepository.findByTicketId(ticketId);
    }

    private void recordEvent(UUID ticketId, String eventType, UUID actorId, Map<String, Object> payload) {
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to serialize ticket event payload", e);
        }
        ticketEventRepository.save(new TicketEvent(ticketId, eventType, actorId, json));
    }
}
