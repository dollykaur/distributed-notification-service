package com.example.notification_worker_email.consumer;

import com.example.notification_worker_email.redis.RedisService;
import com.example.notification_worker_email.repository.NotificationRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.example.notification_worker_email.model.Notification;

@Service
@RequiredArgsConstructor
public class EmailConsumer {

    private final NotificationRepository notificationRepository;
    private final RedisService redisService;
    private final MeterRegistry meterRegistry; // Injected

    // Define counters
    private Counter processedCounter;
    private Counter duplicateCounter;
    private Counter failedCounter;
    private Counter dlqCounter;

    @PostConstruct
    public void initCounters() {
        this.processedCounter = meterRegistry.counter("email.messages.processed");
        this.duplicateCounter = meterRegistry.counter("email.messages.duplicate");
        this.failedCounter = meterRegistry.counter("email.messages.failed");
        this.dlqCounter = meterRegistry.counter("email.messages.dlq");
    }

    //If no manual config beans define, we can use annotation
    //    @Retryable(
    //            value = { Exception.class }, //retry on all exception
    //            maxAttempts = 3, // retry 3 times
    //            backoff = @Backoff(delay = 2000, multiplier = 2)
    //    )
    @KafkaListener(topics="notifications-api", groupId = "email-worker-group")
    public void consume(String message) {
        try {
            System.out.println("Email worker received message :" + message);

            if (message.contains("FAIL")) {
                System.out.println("Simulating failure for message: " + message);
                failedCounter.increment();
                throw new RuntimeException("Forced failure for testing retries/DLQ");
            }

            //Simple parsing if message is in "channel|recipient|text" format
            String[] parts = message.split("\\|");

            if (parts.length != 3 || !parts[0].equals("EMAIL")) {
                dlqCounter.increment(); // send invalid format to DLQ
                throw new IllegalArgumentException("Invalid email message format: " + message);
            }

            String recipient = parts[1];
            String text = parts[2];

            // create a unique message ID
            String messageId = parts[0] + "|" + parts[1] + "|" + parts[2];

            // Check Redis for idempotency
            //Purpose: once a message is processed, we record it in Redis
            //If the same message is retried, isDuplicate() will detect it and skip processing.
            if (redisService.isDuplicate(messageId)) {
                System.out.println("Duplicate message detected. Skipping: " + messageId);
                duplicateCounter.increment();
                return;
            }

            // Simulate sending email
            if (recipient.contains("fail")) {
                throw new RuntimeException("SMTP server timeout for " + recipient);
            }

            // Simulate sending email (replace with SendGrid/Twilio later)
            System.out.println("📨 Sending email to " + recipient + " with text: " + text);

            // Update status in Postgres
            notificationRepository.findAll().forEach(n -> {
                if (n.getRecipient().equals(recipient) && n.getMessage().equals(text)) {
                    n.setStatus("SENT");
                    notificationRepository.save(n);
                }
            });

            // Mark as processed in Redis
            redisService.markProcessed(messageId);
            processedCounter.increment();
        }
        catch (Exception e) {
            failedCounter.increment();
            throw e;
        }
    }
}
