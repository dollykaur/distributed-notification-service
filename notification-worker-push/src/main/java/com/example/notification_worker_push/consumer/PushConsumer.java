package com.example.notification_worker_push.consumer;

import com.example.notification_worker_push.model.Notification;
import com.example.notification_worker_push.redis.RedisService;
import com.example.notification_worker_push.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PushConsumer {

    private final NotificationRepository notificationRepository;
    private final RedisService redisService;

    @KafkaListener(topics="notifications-api", groupId = "push-worker-group")
    public void consume(String message) {
        System.out.println("Push worker received message :" + message);

        //Simple parsing if message is in "channel|recipient|text" format
        String[] parts = message.split("\\|");
        if (parts.length != 3 || !parts[0].equals("PUSH")) {
            System.out.println("Not an PUSH message, ignoring...");
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

        // Simulate sending push (replace with SendGrid/Twilio later)
        System.out.println("📨 Sending push notification to " + recipient + " with text: " + text);

        // Update status in Postgres
        notificationRepository.findFirstByRecipientAndMessageAndChannel(recipient, text, "PUSH")
                .ifPresent((Notification n) -> {
                    n.setStatus("SENT");
                    notificationRepository.save(n);
                });

        // Mark as processed in Redis
        redisService.markProcessed(messageId);
    }
}
