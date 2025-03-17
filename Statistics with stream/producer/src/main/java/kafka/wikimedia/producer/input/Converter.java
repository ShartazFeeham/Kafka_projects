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
        return convert(input, MessageEventWrapper.class);
    }

    public MessageMapWrapper convertToMapObject(String input) {
        return convert(input, MessageMapWrapper.class);
    }

    private  <T> T convert(String input, Class<T> clazz) {
        try {
            T converted = objectMapper.readValue(input, clazz);
            log.info("Converted to Object: {}", converted);
            return converted;
        } catch (Exception e) {
            log.error("Error in converting to Object");
            return null;
        }
    }
}