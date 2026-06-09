package com.example.notification_worker_sms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class NotificationWorkerSmsApplication {

	public static void main(String[] args) {
		// Force JVM timezone to avoid Asia/Calcutta issue
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		SpringApplication.run(NotificationWorkerSmsApplication.class, args);
	}

}
