package com.example.notification_worker_push;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class NotificationWorkerPushApplication {

	public static void main(String[] args) {
		// Force JVM timezone to avoid Asia/Calcutta issue
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		SpringApplication.run(NotificationWorkerPushApplication.class, args);
	}

}
