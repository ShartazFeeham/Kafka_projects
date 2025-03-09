package kafka.livestream.messaging.consumer;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "local.kafka")
public class KafkaConfigProperties {

    private Map<String, String> consumer = new HashMap<>();
    private String bootstrapServers;

    public String getConsumerGroupId() {
        return consumer.getOrDefault("group.id", "default-group");
    }

    public String getKeyDeserializer() {
        return consumer.getOrDefault("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    }

    public String getValueDeserializer() {
        return consumer.getOrDefault("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    }

    public String getAutoOffsetReset() {
        return consumer.getOrDefault("auto.offset.reset", "latest");
    }

}