package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankcluster.infra.SimpleReplier;
import gc.garcol.bankclustercore.ReplyBufferEvent;
import gc.garcol.bankclustercore.ReplyBufferHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Profile({"leader"})
@Component
@RequiredArgsConstructor
public class ReplyBufferHandlerAdapter implements ReplyBufferHandler {

    private final SimpleReplier simpleReplier;

    @Override
    public void onEvent(ReplyBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        Optional.ofNullable(simpleReplier.repliers.get(event.getReplyChannel()))
                .ifPresent(streamObserver -> streamObserver.onNext(
                        BalanceProto.BaseResult.newBuilder()
                                .setCorrelationId(event.getCorrelationId())
                                .setMessage(event.getResult().toString())
                                .build()
                ));
    }
}
