package com.example.notification_worker_email.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final Duration ttl = Duration.ofHours(24); // message cache TTL

    //If we write @RequiredArgsConstructor, then it will create the constructor and we don't have to write it manually
//    public RedisService(RedisTemplate<String, String> redisTemplate) {
//        this.redisTemplate = redisTemplate;
//    }

    public boolean isDuplicate(String messageId) {
        Boolean exists = redisTemplate.hasKey(messageId);
        return exists != null && exists;
    }

    public void markProcessed(String messageId) {
        redisTemplate.opsForValue().set(messageId, "PROCESSED", ttl);
    }
}
