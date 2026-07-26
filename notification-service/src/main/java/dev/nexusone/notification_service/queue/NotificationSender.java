package dev.nexusone.notification_service.queue;

import dev.nexusone.notification_service.model.NotificationLog;
import dev.nexusone.notification_service.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(NotificationSender.class);

    private final ObjectMapper objectMapper;
    private final JavaMailSender mailSender;
    private final RabbitTemplate rabbitTemplate;
    private final NotificationLogRepository logRepository;
    private final String fromAddress;
    private final int maxAttempts;

    public NotificationSender(ObjectMapper objectMapper,
                               JavaMailSender mailSender,
                               RabbitTemplate rabbitTemplate,
                               NotificationLogRepository logRepository,
                               @Value("${nexusone.notifications.from}") String fromAddress,
                               @Value("${nexusone.notifications.max-attempts}") int maxAttempts) {
        this.objectMapper = objectMapper;
        this.mailSender = mailSender;
        this.rabbitTemplate = rabbitTemplate;
        this.logRepository = logRepository;
        this.fromAddress = fromAddress;
        this.maxAttempts = maxAttempts;
    }

    @RabbitListener(queues = RabbitTopologyConfig.QUEUE_SEND)
    public void onJob(String message) {
        NotificationJob job = objectMapper.readValue(message, NotificationJob.class);
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(job.recipient());
            mail.setSubject(job.subject());
            mail.setText(job.body());
            mailSender.send(mail);
            logRepository.save(new NotificationLog(job.ticketId(), job.eventType(), job.recipient(),
                    job.subject(), job.body(), "SENT", null));
        } catch (Exception ex) {
            NotificationJob next = job.nextAttempt();
            if (next.attempts() < maxAttempts) {
                log.warn("Email send failed for ticket {} (attempt {}/{}), scheduling retry",
                        job.ticketId(), next.attempts(), maxAttempts, ex);
                rabbitTemplate.convertAndSend(RabbitTopologyConfig.EXCHANGE, RabbitTopologyConfig.RK_RETRY,
                        objectMapper.writeValueAsString(next));
            } else {
                log.error("Email send exhausted {} attempts for ticket {}, dead-lettering",
                        maxAttempts, job.ticketId(), ex);
                rabbitTemplate.convertAndSend(RabbitTopologyConfig.EXCHANGE, RabbitTopologyConfig.RK_DLQ,
                        objectMapper.writeValueAsString(next));
                logRepository.save(new NotificationLog(job.ticketId(), job.eventType(), job.recipient(),
                        job.subject(), job.body(), "FAILED", ex.getMessage()));
            }
        }
    }
}
