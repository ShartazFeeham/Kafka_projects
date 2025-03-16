package kafka.wikimedia.producer.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
class KafkaStreamsConfig {

    private final KafkaConfigProperties properties;
    private static final Logger logger = LoggerFactory.getLogger(KafkaStreamsConfig.class);
    private final String topic;

    KafkaStreamsConfig(KafkaConfigProperties properties, @Value("${local.kafka-topic-name}") String topic) {
        this.properties = properties;
        this.topic = topic;
    }

    @Bean
    public StreamsConfig kStreamsConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        return new StreamsConfig(props);
    }

    @Bean
    public KStream<String, String> wikimediaProcess(StreamsBuilder builder) {
        Serde<String> stringSerde = Serdes.String();

        KStream<String, String> stream = builder.stream(topic, Consumed.with(stringSerde, stringSerde));

        stream.peek((key, value) -> logger.info("Received message - key: {}, value: {}", key, value))
                .mapValues(value -> {
                    String processedValue = "Processed: " + value;
                    logger.info("Processing message: {}", processedValue);
                    return processedValue;
                })
                .peek((key, value) -> logger.info("Sending processed message - key: {}, value: {}", key, value))
                .to(topic + "-processed", Produced.with(stringSerde, stringSerde));
        return stream;
    }

    @Bean
    public KStream<String, Long> wikimediaCount(StreamsBuilder builder) {
        Serde<String> stringSerde = Serdes.String();
        Serde<Long> longSerde = Serdes.Long();

        KStream<String, String> stream = builder.stream(topic, Consumed.with(stringSerde, stringSerde));
        KTable<String, Long> countTable = stream
                .groupBy((key, value) -> "total", Grouped.with(stringSerde, stringSerde))
                .count();

        KStream<String, Long> countStream = countTable.toStream();
        countStream.peek((key, value) -> logger.info("Total count: {}", value))
                .to(topic + ".count", Produced.with(stringSerde, longSerde));
        return countStream;
    }
}