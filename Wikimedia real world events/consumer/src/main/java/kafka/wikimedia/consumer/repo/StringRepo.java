package kafka.wikimedia.consumer.repo;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface StringRepo extends ElasticsearchRepository<StringModel, String> {
}
