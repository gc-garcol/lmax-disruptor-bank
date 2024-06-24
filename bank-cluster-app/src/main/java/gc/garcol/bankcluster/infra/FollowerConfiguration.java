package gc.garcol.bankcluster.infra;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import gc.garcol.bankcluster.infra.config.ClusterKafkaConfig;
import gc.garcol.bankclustercore.*;
import gc.garcol.bankclustercore.account.AccountRepository;
import gc.garcol.bankclustercore.account.Balances;
import gc.garcol.bankclustercore.cluster.FollowerBootstrap;
import gc.garcol.bankclustercore.cluster.FollowerProperties;
import gc.garcol.bankclustercore.offset.Offset;
import gc.garcol.bankclustercore.offset.SnapshotRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Profile("follower")
@Configuration
@RequiredArgsConstructor
public class FollowerConfiguration {

    private final SnapshotRepository snapshotRepository;
    private final AccountRepository accountRepository;
    private final TransactionManager transactionManager;
    private final ClusterKafkaConfig clusterKafkaConfig;

    private FollowerBootstrap followerBootstrap;

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
    FollowerProperties followerProperties(
        @Value("${follower.bufferSize}") int bufferSize,
        @Value("${follower.pollInterval}") int pollInterval
    ) {
        var learnerProperties = new FollowerProperties();
        learnerProperties.setBufferSize(bufferSize);
        learnerProperties.setPollingInterval(pollInterval);
        return learnerProperties;
    }

    @Bean
    ReplayBufferEventDispatcher replayBufferEventDispatcher(Disruptor<ReplayBufferEvent> replayBufferEventDisruptor) {
        return new ReplayBufferEventDispatcherImpl(replayBufferEventDisruptor);
    }

    @Bean
    ReplayBufferHandler replayBufferHandlerByFollower(CommandHandler commandHandler) {
        return new ReplayBufferHandlerByFollower(commandHandler);
    }

    @Bean
    Disruptor<ReplayBufferEvent> replayBufferEventDisruptor(ReplayBufferHandler replayBufferHandler, FollowerProperties followerProperties) {
        return new ReplayBufferDisruptorDSL(replayBufferHandler).build(followerProperties.getBufferSize(), new SleepingWaitStrategy());
    }

    @Bean
    FollowerBootstrap followerBootstrap(
        StateMachineManager stateMachineManager,
        Disruptor<ReplayBufferEvent> replayBufferEventDisruptor,
        CommandLogConsumerProvider commandLogConsumerProvider,
        Offset offset,
        ReplayBufferEventDispatcher replayBufferEventDispatcher,
        FollowerProperties followerProperties,
        CommandLogKafkaProperties commandLogKafkaProperties
    ) {
        followerBootstrap = new FollowerBootstrap(
            stateMachineManager,
            replayBufferEventDisruptor,
            commandLogConsumerProvider,
            offset,
            replayBufferEventDispatcher,
            followerProperties,
            commandLogKafkaProperties
        );
        return followerBootstrap;
    }

    @EventListener(ApplicationReadyEvent.class)
    void startFollower() {
        log.info("Bootstrapping Follower");
        followerBootstrap.onStart();
    }

    @PreDestroy
    void stopFollower() {
        log.info("Destroying Follower");
        followerBootstrap.onStop();
    }

}
