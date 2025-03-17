package kafka.wikimedia.producer.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
public class TotalCount {

    private final String topic;

    public TotalCount(@Value("${local.kafka-topic-name}") String topic) {
        this.topic = topic;
    }

    /*
    COMMENTING OUT BECAUSE: COUNT IS AN STATEFUL OPERATION AND IT REQUIRES A STATE STORE
    AS A RESULT IT CREATES WEIRD TOPIC THAT IS NOT GOOD LOOKING FOR DEMONSTRATION

    @Bean
    public KStream<String, Long> wikimediaCount(StreamsBuilder streamsBuilder) {
        KStream<String, String> wikiStream = streamsBuilder.stream(topic, Consumed.with(Serdes.String(), Serdes.String()));
        KTable<String, Long> countTable = wikiStream.groupBy((key, value)
                        -> "total", Grouped.with(Serdes.String(), Serdes.String())).count();
        KStream<String, Long> countStream = countTable.toStream();
        countStream.to(topic + "-count", Produced.with(Serdes.String(), Serdes.Long()));
        return countStream;
    }
    */
}
