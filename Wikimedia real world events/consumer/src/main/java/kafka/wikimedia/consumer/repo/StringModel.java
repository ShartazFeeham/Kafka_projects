package kafka.wikimedia.consumer.repo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@Document(indexName = "wikimedia_logs")
public class StringModel {
    @Id
    private String id;
    private String data;
}
