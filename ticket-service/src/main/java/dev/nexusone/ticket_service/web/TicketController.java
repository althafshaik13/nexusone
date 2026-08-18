package dev.nexusone.ticket_service.web;

import dev.nexusone.ticket_service.domain.AppUser;
import dev.nexusone.ticket_service.domain.Ticket;
import dev.nexusone.ticket_service.domain.TicketStatus;
import dev.nexusone.ticket_service.domain.UserRole;
import dev.nexusone.ticket_service.dto.AssignTicketRequest;
import dev.nexusone.ticket_service.dto.CommentResponse;
import dev.nexusone.ticket_service.dto.CreateCommentRequest;
import dev.nexusone.ticket_service.dto.CreateTicketRequest;
import dev.nexusone.ticket_service.dto.TicketEventResponse;
import dev.nexusone.ticket_service.dto.TicketResponse;
import dev.nexusone.ticket_service.dto.TicketStatsResponse;
import dev.nexusone.ticket_service.dto.TransitionTicketRequest;
import dev.nexusone.ticket_service.exception.TicketNotFoundException;
import dev.nexusone.ticket_service.security.AuthenticatedUserResolver;
import dev.nexusone.ticket_service.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final AuthenticatedUserResolver userResolver;

    public TicketController(TicketService ticketService, AuthenticatedUserResolver userResolver) {
        this.ticketService = ticketService;
        this.userResolver = userResolver;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@AuthenticationPrincipal Jwt jwt,
                                                         @Valid @RequestBody CreateTicketRequest request) {
        AppUser currentUser = userResolver.resolve(jwt);
        TicketResponse response = TicketResponse.from(ticketService.createTicket(request, currentUser.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public TicketResponse getTicket(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        AppUser currentUser = userResolver.resolve(jwt);
        Ticket ticket = ticketService.getTicket(id);
        requireVisible(currentUser, ticket);
        return TicketResponse.from(ticket);
    }

    @GetMapping
    public List<TicketResponse> listTickets(@AuthenticationPrincipal Jwt jwt,
                                             @RequestParam(required = false) TicketStatus status,
                                             @RequestParam(required = false) String q) {
        AppUser currentUser = userResolver.resolve(jwt);
        UUID customerScope = currentUser.getRole() == UserRole.CUSTOMER ? currentUser.getId() : null;
        return ticketService.listTickets(status, customerScope, q).stream().map(TicketResponse::from).collect(Collectors.toList());
    }

    @PostMapping("/{id}/assign")
    public TicketResponse assignTicket(@AuthenticationPrincipal Jwt jwt,
                                        @PathVariable UUID id,
                                        @Valid @RequestBody AssignTicketRequest request) {
        AppUser currentUser = userResolver.resolve(jwt);
        return TicketResponse.from(ticketService.assignTicket(id, request.agentId(), currentUser.getId()));
    }

    @PostMapping("/{id}/transition")
    public TicketResponse transitionTicket(@AuthenticationPrincipal Jwt jwt,
                                            @PathVariable UUID id,
                                            @Valid @RequestBody TransitionTicketRequest request) {
        AppUser currentUser = userResolver.resolve(jwt);
        return TicketResponse.from(ticketService.transitionStatus(id, request.targetStatus(), currentUser.getId()));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(@AuthenticationPrincipal Jwt jwt,
                                                        @PathVariable UUID id,
                                                        @Valid @RequestBody CreateCommentRequest request) {
        AppUser currentUser = userResolver.resolve(jwt);
        Ticket ticket = ticketService.getTicket(id);
        requireVisible(currentUser, ticket);
        CommentResponse response = CommentResponse.from(ticketService.addComment(id, request, currentUser.getId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/comments")
    public List<CommentResponse> listComments(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        AppUser currentUser = userResolver.resolve(jwt);
        Ticket ticket = ticketService.getTicket(id);
        requireVisible(currentUser, ticket);
        return ticketService.listComments(id).stream().map(CommentResponse::from).collect(Collectors.toList());
    }

    @GetMapping("/stats")
    public TicketStatsResponse getStats() {
        return ticketService.getStats();
    }

    @GetMapping("/{id}/events")
    public List<TicketEventResponse> listEvents(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        AppUser currentUser = userResolver.resolve(jwt);
        Ticket ticket = ticketService.getTicket(id);
        requireVisible(currentUser, ticket);
        return ticketService.listEvents(id).stream().map(TicketEventResponse::from).collect(Collectors.toList());
    }

    private void requireVisible(AppUser currentUser, Ticket ticket) {
        if (currentUser.getRole() == UserRole.CUSTOMER && !ticket.getCustomerId().equals(currentUser.getId())) {
            throw new TicketNotFoundException(ticket.getId());
        }
    }
}
