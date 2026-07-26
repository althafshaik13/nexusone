package dev.nexusone.notification_service.repository;

import dev.nexusone.notification_service.model.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
    List<NotificationLog> findByTicketId(String ticketId);
}
