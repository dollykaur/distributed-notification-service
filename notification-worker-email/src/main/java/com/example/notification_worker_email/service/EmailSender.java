package com.example.notification_worker_email.service;

public interface EmailSender {
    void sendEmail(String recipient, String message) throws Exception;
}
