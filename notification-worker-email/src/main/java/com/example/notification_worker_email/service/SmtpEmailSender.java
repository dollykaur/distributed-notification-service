package com.example.notification_worker_email.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender{

    private JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String recipient, String message) throws Exception {

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(recipient);
        simpleMailMessage.setSubject("Notification Service");
        simpleMailMessage.setText(message);

        javaMailSender.send(simpleMailMessage);

    }
}
