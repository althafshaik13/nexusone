package dev.nexusone.notification_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notification_logs")
public class NotificationLog {

    @Id
    private String id;
    private String ticketId;
    private String eventType;
    private String recipient;
    private String subject;
    private String body;
    private String status;
    private String error;
    private Instant createdAt;

    public NotificationLog() {
    }

    public NotificationLog(String ticketId, String eventType, String recipient,
                            String subject, String body, String status, String error) {
        this.ticketId = ticketId;
        this.eventType = eventType;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
        this.status = status;
        this.error = error;
        this.createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
