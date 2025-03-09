package kafka.wikimedia.producer.events;

public interface KafkaEventProducer {
    public void publishLogEvent(Object data);
}
