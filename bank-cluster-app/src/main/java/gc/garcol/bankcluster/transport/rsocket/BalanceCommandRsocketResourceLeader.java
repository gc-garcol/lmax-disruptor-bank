package gc.garcol.bankcluster.transport.rsocket;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankcluster.infra.SimpleReplier;
import gc.garcol.bankcluster.transport.CommandBufferEventDispatcherWrapper;
import gc.garcol.bankclustercore.cluster.ClusterStatus;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayDeque;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@Profile("leader")
@Controller
@RequiredArgsConstructor
public class BalanceCommandRsocketResourceLeader {

    private final CommandBufferEventDispatcherWrapper commandBufferEventDispatcherWrapper;
    private final SimpleReplier simpleReplier;

    @PreDestroy
    void destroy() {
        simpleReplier.rsocketRepliers.clear();
    }

    @MessageMapping("balance-command-channel")
    public Flux<BalanceProto.BaseResult> balanceCommandChannel(Flux<BalanceProto.BalanceCommand> commands) {
        if (ClusterStatus.STATE.get().equals(ClusterStatus.NOT_AVAILABLE)) {
            return Flux.just(buildError(503, "Service not available", null));
        }
        var replyChannel = String.format("rsocket-%s", ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
        Sinks.Many<BalanceProto.BaseResult> replySink = Sinks.many().unicast().onBackpressureBuffer(new ArrayDeque<>());
        simpleReplier.rsocketRepliers.put(replyChannel, replySink);
        return commands
            .doOnNext((balanceCommand) -> {
                try {
                    switch (balanceCommand.getTypeCase()) {
                        case CREATEBALANCECOMMAND -> commandBufferEventDispatcherWrapper.create(replyChannel, balanceCommand);
                        case DEPOSITCOMMAND -> commandBufferEventDispatcherWrapper.deposit(replyChannel, balanceCommand);
                        case WITHDRAWCOMMAND -> commandBufferEventDispatcherWrapper.withdraw(replyChannel, balanceCommand);
                        case TRANSFERCOMMAND -> commandBufferEventDispatcherWrapper.transfer(replyChannel, balanceCommand);
                        default -> replySink.tryEmitNext(buildError(400, "Invalid command", null));
                    }
                } catch (Exception e) {
                    replySink.tryEmitNext(buildError(500, "Internal server error", e));
                }
            })
            .doOnTerminate(() -> {
                simpleReplier.rsocketRepliers.remove(replyChannel);
            })
            .thenMany(replySink.asFlux());
    }

    private BalanceProto.BaseResult buildError(int code, String message, Exception e) {
        if (e != null) {
            log.error(message, e);
        }
        return BalanceProto.BaseResult.newBuilder()
            .setCode(code)
            .setMessage(message)
            .build();
    }
}
