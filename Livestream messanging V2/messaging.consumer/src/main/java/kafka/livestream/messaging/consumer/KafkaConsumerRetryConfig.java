package kafka.livestream.messaging.consumer;


import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerRetryConfig {

    private final Logger log = LoggerFactory.getLogger(KafkaConsumerRetryConfig.class);
    private static final long RETRY_INTERVAL = 1000L;  // 1 second
    private static final int MAX_ATTEMPTS = 3;

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        // Configure the recoverer to publish to a DLQ
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
            (record, exception) -> {
                log.error("Failed to process message: {}", record, exception);
                return new TopicPartition(record.topic() + "dead-latter-Q (" + record.topic() + ")", record.partition());
            });

        // Configure back-off policy
        ExponentialBackOff backOff = new ExponentialBackOff(RETRY_INTERVAL, 2);
        backOff.setMaxInterval(RETRY_INTERVAL * 60);

        return new DefaultErrorHandler(recoverer, new FixedBackOff(RETRY_INTERVAL, MAX_ATTEMPTS));
    }
}