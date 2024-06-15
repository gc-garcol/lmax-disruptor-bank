package gc.garcol.bankclustercore;

import gc.garcol.bank.proto.BalanceProto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * @author thaivc
 * @since 2024
 */
@RequiredArgsConstructor
public class CommandBufferJournalerImpl implements CommandBufferJournaler {

    private final KafkaProducer<String, byte[]> producer;
    private final CommandLogKafkaProperties commandLogKafkaProperties;

    private Deque<BalanceProto.CommandLogs> buffers = new ArrayDeque<>();

    {
        buffers.addLast(BalanceProto.CommandLogs.newBuilder().build());
    }

    @Override
    public void onEvent(CommandBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        pushToBuffers(event);
        if (endOfBatch) {
            journalCommandLogs();
        }
    }

    private void pushToBuffers(CommandBufferEvent event) {
        if (buffers.getLast().getLogsList().size() == ClusterConstraint.COMMAND_LOG_CHUNK_SIZE) {
            buffers.addLast(BalanceProto.CommandLogs.newBuilder().build());
        }
        buffers.getLast().getLogsList().add(event.getCommand().getCommandLog());
    }

    @SneakyThrows
    private void journalCommandLogs() {
        for (BalanceProto.CommandLogs buffer : buffers) {
            producer.send(new ProducerRecord<>(commandLogKafkaProperties.getTopic(), buffer.toByteArray())).get();
        }
        buffers.clear();
        buffers.addLast(BalanceProto.CommandLogs.newBuilder().build());
    }
}
