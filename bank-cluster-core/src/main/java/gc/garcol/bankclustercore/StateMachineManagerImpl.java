package gc.garcol.bankclustercore;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankclustercore.account.AccountRepository;
import gc.garcol.bankclustercore.account.Balances;
import gc.garcol.bankclustercore.offset.Offset;
import gc.garcol.bankclustercore.offset.SnapshotRepository;
import gc.garcol.common.exception.BankException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RequiredArgsConstructor
public class StateMachineManagerImpl implements StateMachineManager {

    private final TransactionManager transactionManager;
    private final AccountRepository accountRepository;
    private final SnapshotRepository snapshotRepository;
    private final CommandLogConsumerProvider commandLogConsumerProvider;
    private final CommandHandler commandHandler;
    private final Balances balances;
    private final Offset offset;

    @Setter
    private CommandLogKafkaProperties commandLogKafkaProperties;

    private StateMachineStatus status = StateMachineStatus.INITIALIZING;

    @Override
    public StateMachineStatus getStatus() {
        return status;
    }

    @Override
    public void reloadSnapshot() {
        if (status != StateMachineStatus.INITIALIZING) {
            throw new BankException("Cannot reload snapshot when status is not INITIALIZING");
        }
        status = StateMachineStatus.LOADING_SNAPSHOT;

        accountRepository.balances().forEach(balances::putBalance);
        balances.setLastedId(Optional.ofNullable(accountRepository.lastedId()).orElse(0L));
        offset.setOffset(Optional.ofNullable(snapshotRepository.getLastOffset()).orElse(-1L));
        commandLogKafkaProperties.setNextOffset(offset.nextOffset());

        status = StateMachineStatus.LOADED_SNAPSHOT;
        log.info("Loaded snapshot with offset: {}, lastedId: {}", offset.currentLastOffset(), balances.getLastedId());
    }

    @SneakyThrows
    @Override
    public void replayCommandLogs() {
        if (status != StateMachineStatus.LOADED_SNAPSHOT) {
            throw new BankException("Cannot replay logs when status is not LOADED_SNAPSHOT");
        }
        status = StateMachineStatus.REPLAYING_LOGS;

        try (var consumer = commandLogConsumerProvider.initConsumer(commandLogKafkaProperties)) {
            var partition = new TopicPartition(commandLogKafkaProperties.getTopic(), 0);
            consumer.assign(List.of(partition));
            consumer.seek(partition, offset.nextOffset());
            for (;;) {
                var commandLogsRecords = consumer.poll(Duration.ofMillis(100));
                if (commandLogsRecords.isEmpty()) break;
                for (var commandLogsRecord : commandLogsRecords) {
                    BalanceProto.CommandLogs
                        .parseFrom(commandLogsRecord.value())
                        .getLogsList()
                        .forEach(commandLog -> commandHandler.onCommand(new BaseCommand(commandLog)));
                    offset.setOffset(commandLogsRecord.offset());
                }
            }
            consumer.commitSync();
            status = StateMachineStatus.REPLAYED_LOGS;
        }

        status = StateMachineStatus.ACTIVE;
        log.info("Replayed logs from offset: {}", offset.nextOffset());
    }

    @Override
    public void takeSnapshot() {
        transactionManager.doInNewTransaction(() -> {
            if (status != StateMachineStatus.ACTIVE) {
                throw new BankException("Cannot take snapshot when status is not ACTIVE");
            }
            snapshotRepository.persistLastOffset(offset.currentLastOffset());
            accountRepository.persistBalances(balances.getChangedBalances());
            accountRepository.persistLastId(balances.getLastedId());
            balances.clearChangedBalances();
            log.info("Took snapshot to offset: {}", offset.currentLastOffset());
        });
    }

    @Override
    public void active() {
        status = StateMachineStatus.ACTIVE;
    }
}
