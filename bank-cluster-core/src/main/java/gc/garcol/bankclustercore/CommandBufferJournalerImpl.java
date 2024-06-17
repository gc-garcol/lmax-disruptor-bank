package gc.garcol.bankclustercore;

import gc.garcol.bank.proto.BalanceProto;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CommandBufferJournalerImpl implements CommandBufferJournaler {

    private static final int COMMAND_LOG_CHUNK_SIZE = 100;

    private final KafkaProducer<String, byte[]> producer;
    private final CommandLogKafkaProperties commandLogKafkaProperties;

    private Deque<List<BalanceProto.CommandLog>> buffers = new ArrayDeque<>();

    {
        buffers.add(new ArrayList<>(COMMAND_LOG_CHUNK_SIZE));
    }

    @Override
    public void onEvent(CommandBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        pushToBuffers(event);
        if (endOfBatch) {
            journalCommandLogs();
        }
    }

    private void pushToBuffers(CommandBufferEvent event) {
        if (buffers.getLast().size() == ClusterConstraint.COMMAND_LOG_CHUNK_SIZE) {
            buffers.addLast(new ArrayList<>(COMMAND_LOG_CHUNK_SIZE));
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
        buffers.add(new ArrayList<>(COMMAND_LOG_CHUNK_SIZE));
    }
}
