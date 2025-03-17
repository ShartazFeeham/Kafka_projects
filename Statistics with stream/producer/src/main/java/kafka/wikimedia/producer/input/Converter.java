package kafka.wikimedia.producer.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Converter {

    private final ObjectMapper objectMapper;

    public Converter() {
        this.objectMapper = new ObjectMapper();
    }

    public MessageEventWrapper convertToEventObject(String input) {
        try {
            MessageEventWrapper event = objectMapper.readValue(input, MessageEventWrapper.class);
            log.info("Converted to Object: {}", event);
            return event;
        } catch (Exception e) {
            log.error("Error in converting to Object");
            return null;
        }
    }

    public MessageMapWrapper convertToMapObject(String input) {
        try {
            MessageMapWrapper map = objectMapper.readValue(input, MessageMapWrapper.class);
            log.info("Converted to Object: {}", map);
            return map;
        } catch (Exception e) {
            log.error("Error in converting to Object");
            return null;
        }
    }
}