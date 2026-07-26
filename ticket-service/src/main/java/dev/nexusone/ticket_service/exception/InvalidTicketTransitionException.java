package dev.nexusone.ticket_service.exception;

import dev.nexusone.ticket_service.domain.TicketStatus;

public class InvalidTicketTransitionException extends RuntimeException {
    public InvalidTicketTransitionException(TicketStatus from, TicketStatus to) {
        super("Cannot transition ticket from " + from + " to " + to);
    }
}
