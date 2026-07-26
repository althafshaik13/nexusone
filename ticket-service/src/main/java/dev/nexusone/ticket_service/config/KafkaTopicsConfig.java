package dev.nexusone.ticket_service.config;

import dev.nexusone.ticket_service.messaging.TicketEventPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic ticketCreatedTopic() {
        return TopicBuilder.name(TicketEventPublisher.TOPIC_TICKET_CREATED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic ticketStatusChangedTopic() {
        return TopicBuilder.name(TicketEventPublisher.TOPIC_TICKET_STATUS_CHANGED).partitions(1).replicas(1).build();
    }
}
