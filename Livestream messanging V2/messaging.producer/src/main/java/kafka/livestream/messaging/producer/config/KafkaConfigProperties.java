package kafka.livestream.messaging.producer.config;

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

    private Map<String, String> producer = new HashMap<>();
    private Map<String, String> consumer = new HashMap<>();
    private Map<String, String> admin = new HashMap<>();
    private String bootstrapServers;

}
