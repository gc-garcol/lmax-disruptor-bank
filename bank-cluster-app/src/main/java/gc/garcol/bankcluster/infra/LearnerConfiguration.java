package gc.garcol.bankcluster.infra;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import gc.garcol.bankcluster.infra.config.ClusterKafkaConfig;
import gc.garcol.bankclustercore.*;
import gc.garcol.bankclustercore.account.AccountRepository;
import gc.garcol.bankclustercore.account.Balances;
import gc.garcol.bankclustercore.cluster.LearnerBootstrap;
import gc.garcol.bankclustercore.offset.Offset;
import gc.garcol.bankclustercore.offset.SnapshotRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@Profile("learner")
@Configuration
@RequiredArgsConstructor
public class LearnerConfiguration {

    private final SnapshotRepository snapshotRepository;
    private final AccountRepository accountRepository;
    private final ClusterKafkaConfig clusterKafkaConfig;
    private final TransactionManager transactionManager;

    private LearnerBootstrap learnerBootstrap;

    @Bean
    CommandLogKafkaProperties commandLogKafkaProperties() {
        var properties = new CommandLogKafkaProperties();
        properties.setTopic(clusterKafkaConfig.getTopic());
        properties.setGroupId(clusterKafkaConfig.getGroupId());
        return properties;
    }

    @Bean
    public Balances balances() {
        var balances = new Balances();
        balances.setLastedId(accountRepository.lastedId());
        balances.setEnableChangedCapture(true);
        return balances;
    }

    @Bean
    public Offset offset() {
        var offset = new Offset();
        offset.setOffset(snapshotRepository.getLastOffset());
        return offset;
    }

    @Bean
    CommandHandler commandHandler(Balances balances) {
        return new CommandHandlerImpl(balances);
    }

    @Bean
    StateMachineManager stateMachineManager(CommandLogConsumerProvider commandLogConsumerProvider, CommandHandler commandHandler, Balances balances, Offset offset, CommandLogKafkaProperties commandLogKafkaProperties) {
        var stateMachine = new StateMachineManagerImpl(transactionManager, accountRepository, snapshotRepository, commandLogConsumerProvider, commandHandler, balances, offset);
        stateMachine.setCommandLogKafkaProperties(commandLogKafkaProperties);
        return stateMachine;
    }

    @Bean
    ReplayBufferHandler replayBufferHandlerByLearner(CommandHandler commandHandler, StateMachineManager stateMachineManager) {
        return new ReplayBufferHandlerByLearner(commandHandler, stateMachineManager);
    }

    @Bean
    Disruptor<ReplayBufferEvent> replayBufferEventDisruptor(ReplayBufferHandler replayBufferHandler) {
        return new ReplayBufferDisruptorDSL(replayBufferHandler).build(1 << 5, new SleepingWaitStrategy());
    }

    @Bean
    LearnerBootstrap learnerBootstrap(
        StateMachineManager stateMachineManager,
        Disruptor<ReplayBufferEvent> replayBufferEventDisruptor,
        CommandLogConsumerProvider commandLogConsumerProvider,
        Offset offset,
        CommandHandler commandHandler,
        CommandLogKafkaProperties commandLogKafkaProperties,
        ReplayBufferEventDispatcher replayBufferEventDispatcher
    ) {
        learnerBootstrap = new LearnerBootstrap(
            stateMachineManager,
            replayBufferEventDisruptor,
            commandLogConsumerProvider,
            offset,
            commandHandler,
            replayBufferEventDispatcher
        );
        learnerBootstrap.setCommandLogKafkaProperties(commandLogKafkaProperties);
        return learnerBootstrap;
    }

    @Bean
    ReplayBufferEventDispatcher replayBufferEventDispatcher(Disruptor<ReplayBufferEvent> replayBufferEventDisruptor) {
        return new ReplayBufferEventDispatcherImpl(replayBufferEventDisruptor);
    }

    @EventListener(ApplicationReadyEvent.class)
    void startLeader() {
        log.info("Bootstrapping learner");
        learnerBootstrap.onStart();
    }

    @PreDestroy
    void stopLeader() {
        log.info("Destroying learner");
        learnerBootstrap.onStop();
    }

}
