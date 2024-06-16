package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankcluster.infra.config.ClusterKafkaConfig;
import gc.garcol.bankclustercore.CommandLogConsumerProvider;
import gc.garcol.bankclustercore.CommandLogKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

/**
 * @author thaivc
 * @since 2024
 */
@Component
@RequiredArgsConstructor
public class CommandLogConsumerProviderAdapter implements CommandLogConsumerProvider {

    private final ClusterKafkaConfig clusterKafkaConfig;

    @Override
    public KafkaConsumer<String, byte[]> initConsumer(CommandLogKafkaProperties properties) {
        var props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, clusterKafkaConfig.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        var consumer = new KafkaConsumer<String, byte[]>(props);
        consumer.subscribe(List.of(properties.getTopic()));
        return consumer;
    }
}
