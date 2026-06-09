package com.example.notification_worker_push.repository;

import com.example.notification_worker_push.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findFirstByRecipientAndMessageAndChannel(String recipient, String message, String channel);
}

