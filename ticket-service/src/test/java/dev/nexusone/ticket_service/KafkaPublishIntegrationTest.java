package dev.nexusone.ticket_service;

import dev.nexusone.ticket_service.domain.Ticket;
import dev.nexusone.ticket_service.domain.TicketPriority;
import dev.nexusone.ticket_service.domain.UserRole;
import dev.nexusone.ticket_service.dto.CreateTicketRequest;
import dev.nexusone.ticket_service.dto.CreateUserRequest;
import dev.nexusone.ticket_service.messaging.TicketEventPublisher;
import dev.nexusone.ticket_service.service.TicketService;
import dev.nexusone.ticket_service.service.UserService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class KafkaPublishIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ConsumerFactory<String, String> consumerFactory;

    @Test
    void createTicketPublishesTicketCreatedEvent() {
        UUID customerId = userService.createUser(
                new CreateUserRequest("kafka-test@nexusone.dev", UserRole.CUSTOMER)).getId();
        Ticket ticket = ticketService.createTicket(
                new CreateTicketRequest("Kafka event test", null, TicketPriority.LOW), customerId);

        boolean found = false;
        try (Consumer<String, String> consumer =
                     consumerFactory.createConsumer("it-" + UUID.randomUUID(), null)) {
            consumer.subscribe(List.of(TicketEventPublisher.TOPIC_TICKET_CREATED));
            long deadline = System.currentTimeMillis() + 15000;
            while (System.currentTimeMillis() < deadline && !found) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    if (ticket.getId().toString().equals(record.key())
                            && record.value().contains("TICKET_CREATED")) {
                        found = true;
                    }
                }
            }
        }
        assertTrue(found, "expected a TICKET_CREATED event for ticket " + ticket.getId());
    }
}
