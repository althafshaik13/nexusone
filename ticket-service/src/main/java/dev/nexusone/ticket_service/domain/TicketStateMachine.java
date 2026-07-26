package dev.nexusone.ticket_service.domain;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class TicketStateMachine {

    private static final Map<TicketStatus, Set<TicketStatus>> TRANSITIONS = new EnumMap<>(TicketStatus.class);

    static {
        TRANSITIONS.put(TicketStatus.CREATED, EnumSet.of(TicketStatus.ASSIGNED));
        TRANSITIONS.put(TicketStatus.ASSIGNED, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.ESCALATED));
        TRANSITIONS.put(TicketStatus.IN_PROGRESS, EnumSet.of(TicketStatus.RESOLVED, TicketStatus.ESCALATED));
        TRANSITIONS.put(TicketStatus.ESCALATED, EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED));
        TRANSITIONS.put(TicketStatus.RESOLVED, EnumSet.of(TicketStatus.CLOSED, TicketStatus.REOPENED));
        TRANSITIONS.put(TicketStatus.REOPENED, EnumSet.of(TicketStatus.ASSIGNED, TicketStatus.IN_PROGRESS));
        TRANSITIONS.put(TicketStatus.CLOSED, EnumSet.noneOf(TicketStatus.class));
    }

    private TicketStateMachine() {
    }

    public static boolean canTransition(TicketStatus from, TicketStatus to) {
        return TRANSITIONS.getOrDefault(from, EnumSet.noneOf(TicketStatus.class)).contains(to);
    }

    public static Set<TicketStatus> allowedTransitions(TicketStatus from) {
        return TRANSITIONS.getOrDefault(from, EnumSet.noneOf(TicketStatus.class));
    }
}
