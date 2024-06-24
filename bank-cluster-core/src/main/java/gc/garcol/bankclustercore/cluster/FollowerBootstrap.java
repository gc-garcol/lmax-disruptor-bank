package gc.garcol.bankclustercore.cluster;

import com.lmax.disruptor.dsl.Disruptor;
import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankclustercore.*;
import gc.garcol.bankclustercore.offset.Offset;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.List;

/**
 * @author thaivc
 * @since 2024
 */
@SuppressWarnings("DuplicatedCode")
@Slf4j
@RequiredArgsConstructor
public class FollowerBootstrap implements ClusterBootstrap {

    private final StateMachineManager stateMachineManager;
    private final Disruptor<ReplayBufferEvent> replayBufferEventDisruptor;
    private final CommandLogConsumerProvider commandLogConsumerProvider;
    private final Offset offset;
    private final ReplayBufferEventDispatcher replayBufferEventDispatcher;
    private final FollowerProperties followerProperties;
    private final CommandLogKafkaProperties commandLogKafkaProperties;

    @Override
    public void onStart() {
        try {
            log.info("Follower start");
            loadingStateMachine();
            activeReplayChannel();
            startReplayMessage();
            activeCLuster();
            log.info("Follower started");
        } catch (Exception e) {
            log.error("Follower start failed", e);
            System.exit(-9);
        }
    }

    @Override
    public void onStop() {
        replayBufferEventDisruptor.shutdown();
    }

    private void loadingStateMachine() {
        stateMachineManager.reloadSnapshot();
        stateMachineManager.active();
    }

    private void activeReplayChannel() {
        log.info("On starting command-buffer channel");
        replayBufferEventDisruptor.start();
        log.info("On started command-buffer channel");
    }

    @SneakyThrows
    private void startReplayMessage() {
        log.info("Start replay message");
        new Thread(() -> {
            try (var consumer = commandLogConsumerProvider.initConsumer(commandLogKafkaProperties)) {
                var partition = new TopicPartition(commandLogKafkaProperties.getTopic(), 0);
                consumer.assign(List.of(partition));
                consumer.seek(partition, offset.nextOffset());
                for (; ; ) {
                    var commandLogsRecords = consumer.poll(Duration.ofMillis(followerProperties.getPollingInterval()));
                    for (var commandLogsRecord : commandLogsRecords) {
                        BalanceProto.CommandLogs
                            .parseFrom(commandLogsRecord.value())
                            .getLogsList()
                            .forEach(commandLog -> replayBufferEventDispatcher.dispatch(new ReplayBufferEvent(new BaseCommand(commandLog))));
                        offset.setOffset(commandLogsRecord.offset());
                    }
                    consumer.commitSync();
                }
            } catch (Exception e) {
                log.error("Replay message failed", e);
                System.exit(-9);
            }
        }).start();
    }

    private void activeCLuster() {
        ClusterStatus.STATE.set(ClusterStatus.ACTIVE);
    }
}
