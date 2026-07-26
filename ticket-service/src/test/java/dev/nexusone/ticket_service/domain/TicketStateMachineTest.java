package dev.nexusone.ticket_service.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketStateMachineTest {

    @Test
    void allowsCreatedToAssigned() {
        assertTrue(TicketStateMachine.canTransition(TicketStatus.CREATED, TicketStatus.ASSIGNED));
    }

    @Test
    void rejectsCreatedToResolved() {
        assertFalse(TicketStateMachine.canTransition(TicketStatus.CREATED, TicketStatus.RESOLVED));
    }

    @Test
    void allowsResolvedToReopened() {
        assertTrue(TicketStateMachine.canTransition(TicketStatus.RESOLVED, TicketStatus.REOPENED));
    }

    @Test
    void closedIsTerminal() {
        assertTrue(TicketStateMachine.allowedTransitions(TicketStatus.CLOSED).isEmpty());
    }

    @Test
    void allowsEscalationFromInProgressAndBack() {
        assertTrue(TicketStateMachine.canTransition(TicketStatus.IN_PROGRESS, TicketStatus.ESCALATED));
        assertTrue(TicketStateMachine.canTransition(TicketStatus.ESCALATED, TicketStatus.IN_PROGRESS));
    }
}
