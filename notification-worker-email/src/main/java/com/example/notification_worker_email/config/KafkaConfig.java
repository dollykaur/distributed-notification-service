package com.example.notification_worker_email.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.util.function.BiFunction;

@Configuration
public class KafkaConfig {

    // 1) Recoverer that publishes failed records to "notifications-api.DLQ" (same partition)
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, String> kafkaTemplate) {
        BiFunction<ConsumerRecord<?,?> , Exception, TopicPartition> destResolver =
                (record, ex) -> new TopicPartition("notifications-api.DLQ",
                        record.partition());
        return new DeadLetterPublishingRecoverer(kafkaTemplate, destResolver);
    }

    // 2) DefaultErrorHandler with exponential backoff and the DLQ recoverer
    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        //ExponentialBackOffWithMaxRetries backoff
        ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(3); // 3 retries
        backoff.setInitialInterval(1000L);
        backoff.setMultiplier(2.0);
        backoff.setMaxInterval(10000L);
        DefaultErrorHandler handler = new DefaultErrorHandler(deadLetterPublishingRecoverer, backoff);

        // Example: treat IllegalArgumentException as non-retryable (immediately DLQ)
        handler.addNotRetryableExceptions(IllegalArgumentException.class);

        // Ensure that after recoverer runs (DLQ), the offset is committed so consumer can move forward
        handler.setCommitRecovered(true);

        return handler;
    }

    // 3) Attach the handler to the listener container factory
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory
    (ConsumerFactory<String, String> consumerFactory, DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory);
        kafkaListenerContainerFactory.setCommonErrorHandler(errorHandler);

        // Optional: concurrency if you want multiple threads in the same process
        kafkaListenerContainerFactory.setConcurrency(3);

        return kafkaListenerContainerFactory;
    }
}
