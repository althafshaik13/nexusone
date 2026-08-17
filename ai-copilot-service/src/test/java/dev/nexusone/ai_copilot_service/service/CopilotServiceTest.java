package dev.nexusone.ai_copilot_service.service;

import dev.nexusone.ai_copilot_service.client.OllamaClient;
import dev.nexusone.ai_copilot_service.client.TicketClient;
import dev.nexusone.ai_copilot_service.client.TicketClient.CommentDto;
import dev.nexusone.ai_copilot_service.client.TicketClient.TicketDto;
import dev.nexusone.ai_copilot_service.exception.TicketContextUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CopilotServiceTest {

    private final TicketClient ticketClient = mock(TicketClient.class);
    private final OllamaClient ollamaClient = mock(OllamaClient.class);
    private final CopilotService copilotService = new CopilotService(ticketClient, ollamaClient);

    @Test
    void suggestReplyBuildsContextFromTicketAndComments() {
        UUID ticketId = UUID.randomUUID();
        TicketDto ticket = new TicketDto(ticketId, "Cannot log in", "Getting a 500 error", "IN_PROGRESS", "HIGH",
                UUID.randomUUID(), UUID.randomUUID(), null, Instant.now(), Instant.now());
        CommentDto comment = new CommentDto(UUID.randomUUID(), ticketId, UUID.randomUUID(),
                "Can you try clearing your cookies?", false, Instant.now());
        when(ticketClient.getTicket(any(), anyString())).thenReturn(ticket);
        when(ticketClient.listComments(any(), anyString())).thenReturn(List.of(comment));
        when(ollamaClient.generate(anyString(), contains("Cannot log in"))).thenReturn("Sure, here is a draft reply.");

        String result = copilotService.suggestReply(ticketId, "Bearer token");

        assertThat(result).isEqualTo("Sure, here is a draft reply.");
    }

    @Test
    void suggestReplyWrapsTicketClientFailureAsNotFound() {
        UUID ticketId = UUID.randomUUID();
        when(ticketClient.getTicket(any(), anyString())).thenThrow(new RestClientException("404 Not Found"));

        assertThatThrownBy(() -> copilotService.suggestReply(ticketId, "Bearer token"))
                .isInstanceOf(TicketContextUnavailableException.class);
    }
}
