package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankcluster.infra.SimpleReplier;
import gc.garcol.bankclustercore.ReplyBufferEvent;
import gc.garcol.bankclustercore.ReplyBufferHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Profile({"leader"})
@Slf4j
@Component
@RequiredArgsConstructor
public class ReplyBufferHandlerAdapter implements ReplyBufferHandler {

    private final SimpleReplier simpleReplier;

    @Override
    public void onEvent(ReplyBufferEvent event, long sequence, boolean endOfBatch) throws Exception {

        if (event.getReplyChannel() == null) return;

        switch (event.getReplyChannel().charAt(0)) {
            case 'g' -> Optional.ofNullable(simpleReplier.grpcRepliers.get(event.getReplyChannel()))
                    .ifPresent(streamObserver -> streamObserver.onNext(buildResult(event)));
            case 'r' -> Optional.ofNullable(simpleReplier.rsocketRepliers.get(event.getReplyChannel()))
                    .ifPresent(sinks -> sinks.tryEmitNext(buildResult(event)));
            default -> log.debug(
                "Unknown reply channel: {} | correlationId: {}", event.getReplyChannel(), event.getCorrelationId());
        }
    }

    private BalanceProto.BaseResult buildResult(ReplyBufferEvent event) {
        return BalanceProto.BaseResult.newBuilder()
                .setCorrelationId(event.getCorrelationId())
                .setMessage(event.getResult().toString())
                .build();
    }
}
