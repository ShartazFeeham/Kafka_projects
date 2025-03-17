package kafka.wikimedia.producer.streams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.wikimedia.producer.input.Converter;
import kafka.wikimedia.producer.input.MessageEventWrapper;
import kafka.wikimedia.producer.input.MessageMapWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

import java.util.function.Function;

@Slf4j
@Configuration
@EnableKafkaStreams
public class EventProcessorMultiTopology {

    private final String topic;
    private final Converter converter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EventProcessorMultiTopology(@Value("${local.kafka-topic-name}") String topic, Converter converter) {
        this.topic = topic;
        this.converter = converter;
    }

    @Bean
    public KStream<String, MessageEventWrapper> wikimediaProcessObject(StreamsBuilder streamsBuilder) {
        KStream<String, String> wikiStream = streamsBuilder.stream(topic, Consumed.with(Serdes.String(), Serdes.String()));

        KStream<String, MessageEventWrapper> messageEventStream = wikiStream
                .mapValues(converter::convertToEventObject)
                .filter((key, value) -> value != null);

        KStream<String, MessageMapWrapper> messageMapStream = wikiStream
                .mapValues(converter::convertToMapObject)
                .filter((key, value) -> value != null);

        // Sub-topologies
        publishConvertedEvent(messageEventStream);
        publishAsObjectJson(messageMapStream);
        // publishConvertedCount(messageEventStream); Commented out because it requires a state store
        publishByDomains(messageEventStream);
        publishEventSize(messageEventStream);

        return messageEventStream;
    }

    private void publishConvertedEvent(KStream<String, MessageEventWrapper> messageEventStream) {
        makeStringAndPublish(messageEventStream, event -> topic + "-converted-event");
    }

    private void publishAsObjectJson(KStream<String, MessageMapWrapper> messageMapStream) {
        makeStringAndPublish(messageMapStream, event -> topic + "-converted-map");
    }

    private void publishByDomains(KStream<String, MessageEventWrapper> messageEventStream) {
        makeStringAndPublish(messageEventStream, event -> topic + "-domain-" + event.getMessageEvent().getMeta().getDomain());
    }

    private <T> void makeStringAndPublish(KStream<String, T> messageEventStream, Function<T, String> topicFunction) {
        messageEventStream
                .map((key, event) -> {
                    try {
                        String eventJson = null;
                        try {
                            eventJson = objectMapper.writeValueAsString(event);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        String topic = topicFunction.apply(event);
                        return KeyValue.pair(topic, eventJson);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter((topic, eventJson) -> topic != null && eventJson != null)
                .to((topic, value, recordContext) -> topic, Produced.with(Serdes.String(), Serdes.String()));
    }

    private void publishEventSize(KStream<String, MessageEventWrapper> messageEventStream) {
        messageEventStream
                .mapValues(event -> {
                    String eventName = event.getEvent();
                    int size = event.getMessageEvent().toString().length();
                    return "Size of " + eventName + " event: " + size;
                })
                .to(topic + "-converted-event-size", Produced.with(Serdes.String(), Serdes.String()));
    }

    private void publishConvertedCount(KStream<String, MessageEventWrapper> messageEventStream) {
        messageEventStream
                .groupByKey()
                .count()
                .toStream()
                .mapValues(count -> "Count of converted events: " + count)
                .to(topic + "-converted-event-count", Produced.with(Serdes.String(), Serdes.String()));
    }
}
