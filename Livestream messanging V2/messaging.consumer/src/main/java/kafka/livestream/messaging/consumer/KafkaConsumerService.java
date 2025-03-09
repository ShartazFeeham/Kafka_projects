package kafka.livestream.messaging.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
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

    @KafkaListener(topics = "${local.kafka-topic-name}", groupId = "${local.kafka.consumer.group.id}")
    public void consumeMessage(ConsumerRecord<String, String> record) {
        log.info("Consumed message: Key: {}, Value: {}, Topic: {}, Partition: {}, Offset: {}",
                record.key(), record.value(), record.topic(), record.partition(), record.offset());
        try {
            processMessage(record);
        } catch (Exception e) {
            log.error("Error processing message, simulating failure for retry or DLQ.");
            throw e; // MANDATORY to trigger retry or DLQ
        }
    }

    private void processMessage(ConsumerRecord<String, String> record) {
        if (Math.random() > 0.5) { // 50% chance of failure
            throw new IllegalArgumentException("Simulated processing failure");
        }
        messages.add(record.value());
        log.info("Message processed successfully.");
    }

    public Object receiveMessage() {
        var result = messages.isEmpty() ? "No messages" : new ArrayList<>(messages);
        messages.clear();
        return result;
    }
}
