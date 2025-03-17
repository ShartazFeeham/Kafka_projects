package kafka.wikimedia.producer.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Slf4j
@Configuration
@EnableKafkaStreams
public class PublishAsJson {

    private final String topic;
    private final ObjectMapper mapper;

    public PublishAsJson(@Value("${local.kafka-topic-name}") String topic) {
        this.topic = topic;
        this.mapper = new ObjectMapper();
    }

    @Bean
    public KStream<String, String> wikimediaProcessJson(StreamsBuilder streamsBuilder) {
        KStream<String, String> wikiStream = streamsBuilder.stream(topic, Consumed.with(Serdes.String(), Serdes.String()));
        KStream<String, String> jsonConvertedStream = wikiStream.mapValues((key, value) -> {
            try {
                var json = mapper.writeValueAsString(value);
                log.info("<> Converted to JSON: {}", json);
                return json;
            } catch (Exception e) {
                log.error("<> Error in converting to JSON", e);
                return null;
            }
        });
        jsonConvertedStream.to(topic + "-json", Produced.with(Serdes.String(), Serdes.String()));
        return jsonConvertedStream;
    }
}
