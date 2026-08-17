package dev.nexusone.ai_copilot_service.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class TicketClient {

    private final RestClient restClient;

    public TicketClient(RestClient.Builder builder,
                         @Value("${nexusone.ticket-service.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public TicketDto getTicket(UUID ticketId, String bearerToken) {
        return restClient.get().uri("/api/tickets/{id}", ticketId)
                .header("Authorization", bearerToken).retrieve().body(TicketDto.class);
    }

    public List<CommentDto> listComments(UUID ticketId, String bearerToken) {
        return restClient.get().uri("/api/tickets/{id}/comments", ticketId)
                .header("Authorization", bearerToken).retrieve().body(new org.springframework.core.ParameterizedTypeReference<List<CommentDto>>() {
                });
    }

    public record TicketDto(UUID id, String subject, String description, String status, String priority,
                             UUID customerId, UUID assignedAgentId, Instant slaDueAt, Instant createdAt, Instant updatedAt) {
    }

    public record CommentDto(UUID id, UUID ticketId, UUID authorId, String body, boolean internal, Instant createdAt) {
    }
}
