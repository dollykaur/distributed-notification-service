package com.example.notification_worker_email.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data                 // Generates getters, setters, equals, hashCode, toString
@NoArgsConstructor    // Generates a no-args constructor
@AllArgsConstructor   // Generates an all-args constructor
@Builder              // Generates a builder for fluent object creation
public class Notification {
    @Id
    private Long id; // Must match the id from api
    private String recipient;
    private String message;
    private String channel;
    private String status;
    private LocalDateTime createdAt;
}
