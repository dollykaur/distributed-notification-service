package com.example.notification_worker_sms.repository;

import com.example.notification_worker_sms.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findFirstByRecipientAndMessageAndChannel(String recipient, String text, String message);
}
