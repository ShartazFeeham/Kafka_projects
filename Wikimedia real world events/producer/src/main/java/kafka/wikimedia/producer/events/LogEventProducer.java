package kafka.wikimedia.producer.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogEventProducer implements KafkaEventProducer {
    private final Logger log = LoggerFactory.getLogger(LogEventProducer.class.getSimpleName());

    @Override
    public void publishLogEvent(Object data) {
        // TODO: implement event publishing logic
    }
}
