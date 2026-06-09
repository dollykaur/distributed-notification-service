package com.example.notification_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class NotificationApiApplication {

	public static void main(String[] args) {
		// Force JVM timezone to Kolkata to avoid "Calcutta" being passed
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
		SpringApplication.run(NotificationApiApplication.class, args);
	}

}
