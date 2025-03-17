package kafka.wikimedia.producer.input;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class MessageMapWrapper {
    private Map<String, Object> messageEvent;
    private String event;
}