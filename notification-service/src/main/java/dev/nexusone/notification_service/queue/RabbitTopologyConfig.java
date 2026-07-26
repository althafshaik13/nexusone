package dev.nexusone.notification_service.queue;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTopologyConfig {

    public static final String EXCHANGE = "notification.exchange";
    public static final String QUEUE_SEND = "notification.send";
    public static final String QUEUE_RETRY = "notification.retry";
    public static final String QUEUE_DLQ = "notification.dlq";
    public static final String RK_SEND = "send";
    public static final String RK_RETRY = "retry";
    public static final String RK_DLQ = "dlq";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue sendQueue() {
        return QueueBuilder.durable(QUEUE_SEND).build();
    }

    @Bean
    public Queue retryQueue(@Value("${nexusone.notifications.retry-ttl-ms}") long retryTtlMs) {
        return QueueBuilder.durable(QUEUE_RETRY)
                .ttl((int) retryTtlMs)
                .deadLetterExchange(EXCHANGE)
                .deadLetterRoutingKey(RK_SEND)
                .build();
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean
    public Binding sendBinding() {
        return BindingBuilder.bind(sendQueue()).to(notificationExchange()).with(RK_SEND);
    }

    @Bean
    public Binding retryBinding(Queue retryQueue) {
        return BindingBuilder.bind(retryQueue).to(notificationExchange()).with(RK_RETRY);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(dlqQueue()).to(notificationExchange()).with(RK_DLQ);
    }
}
