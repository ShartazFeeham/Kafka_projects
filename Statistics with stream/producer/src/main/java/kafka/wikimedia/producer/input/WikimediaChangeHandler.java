package kafka.wikimedia.producer.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import com.launchdarkly.eventsource.MessageEvent;
import jakarta.annotation.PostConstruct;
import kafka.wikimedia.producer.events.KafkaEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

@Component
public class WikimediaChangeHandler implements EventHandler {

    public static final String WIKIMEDIA_SOURCE_URL = "https://stream.wikimedia.org/v2/stream/recentchange";
    private final Logger log = LoggerFactory.getLogger(WikimediaChangeHandler.class.getSimpleName());
    private final KafkaEventProducer kafkaEventProducer;

    public WikimediaChangeHandler(KafkaEventProducer kafkaEventProducer) {
        this.kafkaEventProducer = kafkaEventProducer;
    }

    @Override
    public void onOpen() {}

    @Override
    public void onClosed() {}

    @Override
    public void onMessage(String event, MessageEvent messageEvent) {
        Object data = transformIncomingData(event, messageEvent.getData());
        log.info("Incoming log data: {}", data);
        kafkaEventProducer.publishLogEvent(data);
    }

    private Object transformIncomingData(String event, String data) {
        try {
            var result = new ObjectMapper().readValue(data, Object.class);
            return Map.of("event", event, "messageEvent", result);
        } catch (JsonProcessingException e) {
            log.error("Error in transforming incoming data: {}", data);
            return Map.of("event", event, "messageEvent", data);
        }
    }

    @Override
    public void onComment(String comment) {}

    @Override
    public void onError(Throwable t) {
        log.error("Error in Stream Reading", t);
    }

    @PostConstruct
    public void init() {
        log.info("WikimediaChangeHandler initialized");
        log.info("-----------STARTING TO CONSUME LOGS FROM WIKIMEDIA-----------");

        new EventSource.Builder(this, URI.create(WIKIMEDIA_SOURCE_URL))
                .build()
                .start();
    }
}