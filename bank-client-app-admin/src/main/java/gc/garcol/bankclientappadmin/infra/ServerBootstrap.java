package gc.garcol.bankclientappadmin.infra;

import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import gc.garcol.bank.proto.BalanceCommandServiceGrpc;
import gc.garcol.bank.proto.BalanceQueryServiceGrpc;
import gc.garcol.bankclientcore.cluster.*;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * @author thaivc
 * @since 2024
 */
@Configuration
public class ServerBootstrap {
    @Value("${cluster.leader.host}")
    private String leaderHost;

    @Value("${cluster.leader.port}")
    private int leaderPort;

    @Value("${request-buffer.pow}")
    private int requestBufferPow;

    private Disruptor<RequestBufferEvent> balanceCommandBufferDisruptor;

    @EventListener(ApplicationReadyEvent.class)
    void startServer() {
        balanceCommandBufferDisruptor.start();
    }

    @Bean
    BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub() {
        return BalanceCommandServiceGrpc.newStub(ManagedChannelBuilder.forAddress(leaderHost, leaderPort).usePlaintext().build());
    }

    @Bean
    BalanceQueryServiceGrpc.BalanceQueryServiceStub balanceQueryServiceStub() {
        return BalanceQueryServiceGrpc.newStub(ManagedChannelBuilder.forAddress(leaderHost, leaderPort).usePlaintext().build());
    }

    @Bean
    Disruptor<RequestBufferEvent> balanceCommandBufferDisruptor(BalanceRequestBufferHandler balanceRequestBufferHandler) {
        balanceCommandBufferDisruptor = new RequestBufferDisruptorDSL(balanceRequestBufferHandler).build(1 << requestBufferPow, new SleepingWaitStrategy());
        return balanceCommandBufferDisruptor;
    }

    @Bean
    RequestBufferDispatcher<BaseRequest> requestBufferDispatcher(
        Disruptor<RequestBufferEvent> balanceCommandBufferDisruptor,
        @Value("${request.timeout.milliseconds}") long timeoutMilliseconds
    ) {
        return new RequestBufferDispatcherImpl<>(balanceCommandBufferDisruptor, timeoutMilliseconds);
    }

    @Bean
    BalanceCommandStub balanceCommandStub(BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub) {
        return new BalanceCommandStub(balanceCommandServiceStub);
    }

    @Bean
    BalanceQueryStub balanceQueryStub(BalanceQueryServiceGrpc.BalanceQueryServiceStub balanceQueryServiceStub) {
        return new BalanceQueryStub(balanceQueryServiceStub);
    }

    @Bean
    BalanceRequestBufferHandler balanceRequestBufferHandler(BalanceCommandStub balanceCommandStub, BalanceQueryStub balanceQueryStub) {
        return new BalanceRequestBufferHandler(balanceCommandStub, balanceQueryStub);
    }
}
