package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankcluster.infra.config.ClusterKafkaConfig;
import gc.garcol.bankclustercore.CommandLogKafkaProperties;
import gc.garcol.bankclustercore.CommandLogProducerProvider;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author thaivc
 * @since 2024
 */
@Profile("leader")
@Component
@RequiredArgsConstructor
public class CommandLogProducerProviderAdapter implements CommandLogProducerProvider {

    private final ClusterKafkaConfig clusterKafkaConfig;

    @Override
    public KafkaProducer<String, byte[]> initProducer(CommandLogKafkaProperties properties) {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, clusterKafkaConfig.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        return new KafkaProducer<>(props);
    }
}
