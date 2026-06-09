package com.example.notification_worker_email.repository;

import com.example.notification_worker_email.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
