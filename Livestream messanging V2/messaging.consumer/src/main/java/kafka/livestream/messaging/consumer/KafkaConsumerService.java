package kafka.livestream.messaging.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KafkaConsumerService {

    private final List<Object> messages = new ArrayList<>();
    private final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "${local.kafka-topic-name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String message) {
        log.info("Consumed message: {}", message);
        messages.add(message);
    }

    public Object receiveMessage() {
        var result = messages.isEmpty() ? "No messages" : new ArrayList<>(messages);
        messages.clear();
        return result;
    }
}
