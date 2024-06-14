package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankclustercore.BaseCommand;
import gc.garcol.bankclustercore.CommandLogConsumerProvider;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.List;

public class CommandLogConsumerProviderAdapter implements CommandLogConsumerProvider {
    @Override
    public KafkaConsumer<String, List<BaseCommand>> initConsumer(String topic, String groupId) {
        return null;
    }
}
