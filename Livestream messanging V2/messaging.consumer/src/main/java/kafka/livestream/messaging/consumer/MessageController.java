package kafka.livestream.messaging.consumer;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {
    private final KafkaConsumerService consumerService;

    public MessageController(KafkaConsumerService consumerService) {
        this.consumerService = consumerService;
    }

    @GetMapping
    public ResponseEntity<?> getMessage() {
        return ResponseEntity.ok(consumerService.receiveMessage());
    }
}
