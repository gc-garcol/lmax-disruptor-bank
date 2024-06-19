package gc.garcol.bankclustercore;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankclustercore.cluster.LeaderProperties;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * @author thaivc
 * @since 2024
 */
public class CommandBufferJournalerImpl implements CommandBufferJournaler {

    private final KafkaProducer<String, byte[]> producer;
    private final CommandLogKafkaProperties commandLogKafkaProperties;
    private final LeaderProperties leaderProperties;

    private final Deque<List<BalanceProto.CommandLog>> buffers = new ArrayDeque<>();

    public CommandBufferJournalerImpl(
        KafkaProducer<String, byte[]> producer,
        CommandLogKafkaProperties commandLogKafkaProperties,
        LeaderProperties leaderProperties
    ) {
        this.producer = producer;
        this.commandLogKafkaProperties = commandLogKafkaProperties;
        this.leaderProperties = leaderProperties;

        buffers.add(new ArrayList<>(leaderProperties.getLogsChunkSize()));
    }

    @Override
    public void onEvent(CommandBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        pushToBuffers(event);
        if (endOfBatch) {
            journalCommandLogs();
        }
    }

    private void pushToBuffers(CommandBufferEvent event) {
        if (buffers.getLast().size() == leaderProperties.getLogsChunkSize()) {
            buffers.addLast(new ArrayList<>(leaderProperties.getLogsChunkSize()));
        }
        buffers.getLast().add(event.getCommand().getCommandLog());
    }

    @SneakyThrows
    private void journalCommandLogs() {
        for (List<BalanceProto.CommandLog> commandLogs : buffers) {
            var commandLogsMessage = BalanceProto.CommandLogs.newBuilder()
                .addAllLogs(commandLogs)
                .build();
            producer.send(new ProducerRecord<>(commandLogKafkaProperties.getTopic(), commandLogsMessage.toByteArray())).get();
        }
        buffers.clear();
        buffers.add(new ArrayList<>(leaderProperties.getLogsChunkSize()));
    }
}
