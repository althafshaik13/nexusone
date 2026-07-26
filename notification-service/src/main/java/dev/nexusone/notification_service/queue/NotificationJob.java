package dev.nexusone.notification_service.queue;

public record NotificationJob(
        String ticketId,
        String eventType,
        String recipient,
        String subject,
        String body,
        int attempts
) {
    public NotificationJob nextAttempt() {
        return new NotificationJob(ticketId, eventType, recipient, subject, body, attempts + 1);
    }
}
