package kafka.wikimedia.producer.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class LogEventProducer implements KafkaEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Logger log = LoggerFactory.getLogger(LogEventProducer.class);
    private final String topic;
    private final Random random = new Random();
    private final ObjectMapper mapper = new ObjectMapper();

    public LogEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                                @Value("${local.kafka-topic-name}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publishLogEvent(Object data) {
        try {
            var json = mapper.writeValueAsString(data);
            sendToBroker(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendToBroker(String data) {
        kafkaTemplate.send(topic, data).whenCompleteAsync((result, exception) -> {
            if (exception != null) {
                log.error("Error in sending data to Kafka broker", exception);
            } else {
                log.info("Data sent to Kafka broker: {}", result);
            }
        });
    }
}
