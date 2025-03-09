package kafka.wikimedia.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.wikimedia.consumer.repo.StringModel;
import kafka.wikimedia.consumer.repo.StringRepo;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final List<Object> messages = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final StringRepo repo;

    @KafkaListener(topics = "${local.kafka-topic-name}", groupId = "${local.kafka.consumer.group.id}")
    public void consumeMessage(ConsumerRecord<String, String> record) {
        log.info("Consumed message: Key: {}, Value: {}, Topic: {}, Partition: {}, Offset: {}",
                record.key(), record.value(), record.topic(), record.partition(), record.offset());
        try {
            sendToElasticSearch(record);
        } catch (Exception e) {
            log.error("Error processing message, simulating failure for retry or DLQ.");
            throw e; // MANDATORY to trigger retry or DLQ
        }
    }

    private void sendToElasticSearch(ConsumerRecord<String, String> record) {
        try {
            var data = mapper.writeValueAsString(record.value());
            var result = repo.save(new StringModel(UUID.randomUUID().toString(), data));
            log.info("Sent message to ElasticSearch: {}", result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}