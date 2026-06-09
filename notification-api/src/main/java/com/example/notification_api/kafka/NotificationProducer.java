package com.example.notification_api.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.swing.plaf.synth.SynthOptionPaneUI;

@Service
public class NotificationProducer {

    private static final String TOPIC = "notification";
    private final KafkaTemplate<String, String> kafkaTemplate;


    public NotificationProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
        System.out.println("Sent messages to kafka" + message);
    }
}
