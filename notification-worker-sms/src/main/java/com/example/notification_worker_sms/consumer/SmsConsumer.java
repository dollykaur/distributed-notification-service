package com.example.notification_worker_sms.consumer;

import com.example.notification_worker_sms.model.Notification;
import com.example.notification_worker_sms.redis.RedisService;
import com.example.notification_worker_sms.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
//Automatically injects notificationRepository via constructor.
@RequiredArgsConstructor
public class SmsConsumer {

    private final NotificationRepository notificationRepository;
    private final RedisService redisService;

    //Subscribes to topic notifications-api.
    //Belongs to consumer group sms-worker-group (separate from email-worker-group).
    //Logs any message it receives.
    @KafkaListener(topics="notifications-api", groupId = "sms-worker-group")
    public void consume(String message) {
        System.out.println("Sms worker received message :" + message);

        //Simple parsing if message is in "channel|recipient|text" format
        String[] parts = message.split("\\|");
        if(parts.length != 3 || !parts[0].equals("SMS")) {
            System.out.println("Not an sms message, ignoring...");
            return;
        }

        String recipient = parts[1];
        String text = parts[2];

        // create a unique message ID
        String messageId = parts[0] + "|" + parts[1] + "|" + parts[2];

        // Check Redis for idempotency
        if(redisService.isDuplicate(messageId)) {
            System.out.println("Duplicate message detected. Skipping: " + messageId);
            return;
        }

        // Simulate sending sms (replace with SendGrid/Twilio later)
        System.out.println("📨 Sending sms to " + recipient + " with text: " + text);

        // Update status in Postgres
        notificationRepository.findFirstByRecipientAndMessageAndChannel(recipient, text, "SMS")
                .ifPresent(n -> {
                    n.setStatus("SENT");
                    notificationRepository.save(n);
                });

        // Mark as processed in Redis
        redisService.markProcessed(messageId);
    }
}
