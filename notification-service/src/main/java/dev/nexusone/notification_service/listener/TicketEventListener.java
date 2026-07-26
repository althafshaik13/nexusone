package dev.nexusone.notification_service.listener;

import dev.nexusone.notification_service.queue.NotificationJob;
import dev.nexusone.notification_service.queue.RabbitTopologyConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class TicketEventListener {

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final String supportInbox;

    public TicketEventListener(ObjectMapper objectMapper,
                                RabbitTemplate rabbitTemplate,
                                @Value("${nexusone.notifications.support-inbox}") String supportInbox) {
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
        this.supportInbox = supportInbox;
    }

    @KafkaListener(topics = {"ticket.created", "ticket.status.changed"})
    public void onTicketEvent(String message) {
        TicketEventPayload event = objectMapper.readValue(message, TicketEventPayload.class);
        String subject = "[NexusOne] Ticket " + event.ticketId() + ": " + event.eventType();
        NotificationJob job = new NotificationJob(
                event.ticketId(), event.eventType(), supportInbox, subject, buildBody(event), 0);
        rabbitTemplate.convertAndSend(RabbitTopologyConfig.EXCHANGE, RabbitTopologyConfig.RK_SEND,
                objectMapper.writeValueAsString(job));
    }

    private String buildBody(TicketEventPayload event) {
        StringBuilder sb = new StringBuilder();
        sb.append("Event: ").append(event.eventType()).append('\n');
        sb.append("Ticket: ").append(event.ticketId()).append('\n');
        sb.append("Status: ").append(event.status()).append('\n');
        if (event.previousStatus() != null) {
            sb.append("Previous status: ").append(event.previousStatus()).append('\n');
        }
        sb.append("Priority: ").append(event.priority()).append('\n');
        sb.append("Customer: ").append(event.customerId()).append('\n');
        if (event.assignedAgentId() != null) {
            sb.append("Assigned agent: ").append(event.assignedAgentId()).append('\n');
        }
        sb.append("Occurred at: ").append(event.occurredAt());
        return sb.toString();
    }
}
