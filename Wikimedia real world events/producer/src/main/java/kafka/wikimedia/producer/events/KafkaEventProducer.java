package kafka.wikimedia.producer.events;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface KafkaEventProducer {
    void publishLogEvent(Object data);
}
