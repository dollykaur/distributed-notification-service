package com.example.notification_api.service;


import com.example.notification_api.kafka.NotificationProducer;
import com.example.notification_api.model.Notification;
import com.example.notification_api.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationProducer notificationProducer;
    private final NotificationRepository notificationRepository;


    public NotificationService(NotificationProducer notificationProducer, NotificationRepository notificationRepository) {
        this.notificationProducer = notificationProducer;
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(Notification notification) {
        //Save notification request to postgres
        Notification savedMessage = notificationRepository.save(notification);

        //Send or publish message to kafka
        String message = String.format("%s|%s|%s", savedMessage.getChannel(), savedMessage.getMessage(), savedMessage.getRecipient());
        notificationProducer.sendMessage(message);

        return savedMessage;
    }
}
