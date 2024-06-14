package gc.garcol.bankclustercore;

import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.List;

public interface CommandLogConsumerProvider {

    KafkaConsumer<String, List<BaseCommand>> initConsumer(String topic, String groupId);

}
