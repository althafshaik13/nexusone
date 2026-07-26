package dev.nexusone.ticket_service.dto;

import dev.nexusone.ticket_service.domain.TicketComment;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID ticketId,
        UUID authorId,
        String body,
        boolean internal,
        Instant createdAt
) {
    public static CommentResponse from(TicketComment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getTicketId(),
                comment.getAuthorId(),
                comment.getBody(),
                comment.isInternal(),
                comment.getCreatedAt()
        );
    }
}
