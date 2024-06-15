package gc.garcol.bankclustercore;

import org.apache.kafka.clients.producer.KafkaProducer;

/**
 * @author thaivc
 * @since 2024
 */
public interface CommandLogProducerProvider {
    KafkaProducer<String, byte[]> initProducer(CommandLogKafkaProperties properties);
}
