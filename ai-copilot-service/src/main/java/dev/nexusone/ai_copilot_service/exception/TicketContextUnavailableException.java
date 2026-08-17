package dev.nexusone.ai_copilot_service.exception;

import java.util.UUID;

public class TicketContextUnavailableException extends RuntimeException {
    public TicketContextUnavailableException(UUID ticketId) {
        super("Could not load ticket " + ticketId + " to build AI context");
    }
}
