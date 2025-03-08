package kafka.livestream.messaging.producer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AdminClient adminClient;
    private final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private final String topicName;
    private final int numPartitions;
    private final short replicationFactor;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate, AdminClient adminClient,
                                @Value("${local.kafka-topic-name}") String topicName,
                                @Value("${local.kafka-topic-partitions}") int numPartitions,
                                @Value("${local.kafka-topic-replication-factor}") short replicationFactor) {
        this.kafkaTemplate = kafkaTemplate;
        this.adminClient = adminClient;
        this.topicName = topicName;
        this.numPartitions = numPartitions;
        this.replicationFactor = replicationFactor;
    }

    public void sendMessage(String message) {
        // createTopicIfNotExists(topicName, numPartitions, replicationFactor);
        send(new ProducerRecord<>(topicName, message));
    }

    // @WARNING: This approach seems to be an overkill for creating a topic.
    public void createTopicIfNotExists(String topicName, int numPartitions, short replicationFactor) {
        try {
            NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            log.info("Topic {} created successfully", topicName);
        } catch (Exception e) {
            if (e.getCause().getMessage().contains("already exists")) {
                log.info("Topic {} already exists", topicName);
            }
            else {
                log.error("Failed to create topic: {}", topicName, e);
            }
        }
    }

    protected void send(ProducerRecord<String, String> record) {
        log.info("Attempting to send message. Key: {}, Value: {}, Topic: {}, Partition: {}, Headers: {}, Timestamp: {}",
                record.key(), record.value(), record.topic(), record.partition(), record.headers(), record.timestamp());

        kafkaTemplate.send(record)
                .whenCompleteAsync((resultMetadata, exception) -> {
                    if (exception != null) {
                        log.error("Failed to send message!", exception);
                    }
                    else {
                        log.info("Message sent successfully. Key: {}, Value: {}, topic: {}, Partition: {}, Headers: {}, " +
                                        "Timestamp: {}, Offset: {}", resultMetadata.getProducerRecord().key(),
                                resultMetadata.getProducerRecord().value(), resultMetadata.getProducerRecord().topic(),
                                resultMetadata.getProducerRecord().partition(), resultMetadata.getProducerRecord().headers(),
                                resultMetadata.getProducerRecord().timestamp(), resultMetadata.getRecordMetadata().offset());
                    }
                });
    }
}
