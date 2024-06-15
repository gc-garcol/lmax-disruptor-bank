package gc.garcol.bankclustercore;

import org.apache.kafka.clients.consumer.KafkaConsumer;

/**
 * @author thaivc
 * @since 2024
 */
public interface CommandLogConsumerProvider {
    KafkaConsumer<String, byte[]> initConsumer(CommandLogKafkaProperties properties);
}
