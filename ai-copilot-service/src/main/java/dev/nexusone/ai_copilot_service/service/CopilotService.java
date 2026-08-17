package dev.nexusone.ai_copilot_service.service;

import dev.nexusone.ai_copilot_service.client.OllamaClient;
import dev.nexusone.ai_copilot_service.client.TicketClient;
import dev.nexusone.ai_copilot_service.client.TicketClient.CommentDto;
import dev.nexusone.ai_copilot_service.client.TicketClient.TicketDto;
import dev.nexusone.ai_copilot_service.exception.TicketContextUnavailableException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

@Service
public class CopilotService {

    private static final String SUGGEST_REPLY_SYSTEM_PROMPT = """
            You are an assistant helping a support agent at NexusOne draft a reply to a customer.
            Read the ticket and the comment thread below, then draft a single reply the agent could
            send to the customer as-is or lightly edit. Be professional, concise, and specific to
            what is actually in the thread. Do not invent facts, policies, or commitments that
            are not supported by the ticket content. Do not include a greeting/signature block
            unless the existing thread's tone calls for one. Output only the reply text, nothing else.
            """;

    private static final String SUMMARIZE_SYSTEM_PROMPT = """
            You are an assistant helping a support agent at NexusOne quickly understand a ticket.
            Read the ticket and the comment thread below, then produce a short summary (3-5 bullet
            points) covering: the customer's issue, key facts/details mentioned, what has already
            been tried or said, and the current state. Be concise and factual. Output only the
            summary, nothing else.
            """;

    private final TicketClient ticketClient;
    private final OllamaClient ollamaClient;

    public CopilotService(TicketClient ticketClient, OllamaClient ollamaClient) {
        this.ticketClient = ticketClient;
        this.ollamaClient = ollamaClient;
    }

    public String suggestReply(UUID ticketId, String bearerToken) {
        String context = buildContext(ticketId, bearerToken);
        return ollamaClient.generate(SUGGEST_REPLY_SYSTEM_PROMPT, context);
    }

    public String summarize(UUID ticketId, String bearerToken) {
        String context = buildContext(ticketId, bearerToken);
        return ollamaClient.generate(SUMMARIZE_SYSTEM_PROMPT, context);
    }

    private String buildContext(UUID ticketId, String bearerToken) {
        TicketDto ticket;
        List<CommentDto> comments;
        try {
            ticket = ticketClient.getTicket(ticketId, bearerToken);
            comments = ticketClient.listComments(ticketId, bearerToken);
        } catch (RestClientException e) {
            throw new TicketContextUnavailableException(ticketId);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Subject: ").append(ticket.subject()).append('\n');
        sb.append("Status: ").append(ticket.status()).append('\n');
        sb.append("Priority: ").append(ticket.priority()).append('\n');
        sb.append("Description: ").append(ticket.description() == null ? "(none)" : ticket.description()).append('\n');
        sb.append('\n').append("Comment thread (oldest first):\n");
        if (comments.isEmpty()) {
            sb.append("(no comments yet)\n");
        } else {
            int i = 1;
            for (CommentDto comment : comments) {
                sb.append(i++).append(". [").append(comment.internal() ? "internal note" : "visible to customer")
                        .append("] ").append(comment.body()).append('\n');
            }
        }
        return sb.toString();
    }
}
