package kafka.livestream.messaging.producer;

import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessageController {

    private final KafkaProducerService producerService;

    public MessageController(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping
    public void publish(@RequestBody @NonNull String message) {
        producerService.sendMessage(message);
    }
}