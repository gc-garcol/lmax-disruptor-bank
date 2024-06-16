package gc.garcol.bankcluster.infra;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import gc.garcol.bankcluster.infra.adapter.CommandLogConsumerProviderAdapter;
import gc.garcol.bankcluster.infra.config.ClusterKafkaConfig;
import gc.garcol.bankclustercore.*;
import gc.garcol.bankclustercore.account.AccountRepository;
import gc.garcol.bankclustercore.account.Balances;
import gc.garcol.bankclustercore.cluster.LeaderBootstrap;
import gc.garcol.bankclustercore.offset.Offset;
import gc.garcol.bankclustercore.offset.SnapshotRepository;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

/**
 * @author thaivc
 * @since 2024
 */
@Profile("leader")
@Configuration
@RequiredArgsConstructor
public class LeaderConfiguration {

    private static final Logger log = LoggerFactory.getLogger(LeaderConfiguration.class);
    private final ClusterKafkaConfig clusterKafkaConfig;
    private final TransactionManager transactionManager;
    private final SnapshotRepository snapshotRepository;
    private final AccountRepository accountRepository;
    private final CommandLogProducerProvider commandLogProducerProvider;

    private LeaderBootstrap leaderBootstrap;

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
    KafkaProducer<String, byte[]> producer() {
        return commandLogProducerProvider.initProducer(null);
    }

    @Bean
    CommandLogConsumerProvider commandLogConsumerProvider() {
        return new CommandLogConsumerProviderAdapter(clusterKafkaConfig);
    }

    @Bean
    CommandHandler commandHandler(Balances balances) {
        return new CommandHandlerImpl(balances);
    }

    @Bean
    CommandBufferJournaler commandBufferJournaler(KafkaProducer<String, byte[]> producer, CommandLogKafkaProperties commandLogKafkaProperties) {
        return new CommandBufferJournalerImpl(producer, commandLogKafkaProperties);
    }

    @Bean
    CommandBufferHandler commandBufferHandler(CommandHandler commandHandler) {
        return new CommandBufferHandlerImpl(commandHandler);
    }

    @Bean
    ReplyBufferEventDispatcher replyBufferEventDispatcher() {
        return new ReplyBufferEventDispatcherImpl();
    }

    @Bean
    CommandBufferReply commandBufferReply(ReplyBufferEventDispatcher replyBufferEventDispatcher) {
        return new CommandBufferReplyImpl(replyBufferEventDispatcher);
    }

    @Bean
    Disruptor<CommandBufferEvent> commandBufferDisruptor(CommandBufferJournaler commandBufferJournaler, CommandBufferHandler commandBufferHandler, CommandBufferReply commandBufferReply) {
        return new CommandBufferDisruptorDSL(commandBufferJournaler, commandBufferHandler, commandBufferReply).build(1 << 15, new YieldingWaitStrategy());
    }

    @Bean
    ReplyBufferHandler replyBufferHandler() {
        return new ReplyBufferHandlerImpl();
    }

    @Bean
    Disruptor<ReplyBufferEvent> replyBufferEventDisruptor(ReplyBufferHandler replyBufferHandler) {
        return new ReplyBufferDisruptorDSL(replyBufferHandler).build(1 << 16, new YieldingWaitStrategy());
    }

    @Bean
    StateMachineManager stateMachineManager(CommandLogConsumerProvider commandLogConsumerProvider, CommandHandler commandHandler, Balances balances, Offset offset, CommandLogKafkaProperties commandLogKafkaProperties) {
        var stateMachine = new StateMachineManagerImpl(transactionManager, accountRepository, snapshotRepository, commandLogConsumerProvider, commandHandler, balances, offset);
        stateMachine.setCommandLogKafkaProperties(commandLogKafkaProperties);
        return stateMachine;
    }

    @Bean
    LeaderBootstrap leaderBootstrap(StateMachineManager stateMachineManager, Disruptor<CommandBufferEvent> commandBufferDisruptor, Disruptor<ReplyBufferEvent> replyBufferEventDisruptor) {
        leaderBootstrap = new LeaderBootstrap(stateMachineManager, commandBufferDisruptor, replyBufferEventDisruptor);
        return leaderBootstrap;
    }

    @EventListener(ApplicationReadyEvent.class)
    void startLeader() {
        log.info("Bootstrapping leader");
        leaderBootstrap.onStart();
    }

    @PreDestroy
    void stopLeader() {
        log.info("Destroying leader");
        leaderBootstrap.onStop();
    }

}
