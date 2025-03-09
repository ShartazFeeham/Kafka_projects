package kafka.livestream.messaging.consumer;


import org.apache.kafka.clients.consumer.ConsumerRecord;
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
    private final KafkaConfigProperties prop;

    public KafkaConsumerRetryConfig(KafkaConfigProperties kafkaProperties) {
        this.prop = kafkaProperties;
    }

    @Bean
    public CommonErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        // Configure the recoverer to publish to a DLQ
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
            (record, exception) -> {
                log.error("Failed to process message: {}", record, exception);
                return new TopicPartition(generateTopicName(record), record.partition());
            });

        // Configure back-off policy
        ExponentialBackOff backOff = new ExponentialBackOff(prop.getRetryInterval(), prop.getRetryBackoffMultiplier());
        backOff.setMaxInterval(prop.getRetryBackoffMaxInterval());

        return new DefaultErrorHandler(recoverer, new FixedBackOff(prop.getRetryInterval(), prop.getRetryAttempts()));
    }

    private String generateTopicName(ConsumerRecord<?, ?> record) {
        return prop.getDlqPrefix() + record.topic() + prop.getDlqSuffix();
    }
}