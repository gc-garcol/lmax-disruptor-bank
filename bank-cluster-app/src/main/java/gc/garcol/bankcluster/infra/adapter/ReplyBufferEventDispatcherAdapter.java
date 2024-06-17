package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankcluster.infra.SimpleReplier;
import gc.garcol.bankclustercore.ReplyBufferEvent;
import gc.garcol.bankclustercore.ReplyBufferEventDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Replace default implementation of ReplyBufferEventDispatcher
 *
 * @author thaivc
 * @since 2024
 */
@Component
@RequiredArgsConstructor
public class ReplyBufferEventDispatcherAdapter implements ReplyBufferEventDispatcher {

    private final SimpleReplier simpleReplier;

    @Override
    public void dispatch(ReplyBufferEvent event) {
        simpleReplier.repliers.get(event.getReplyChannel()).onNext(
            BalanceProto.BaseResult.newBuilder()
                .setCorrelationId(event.getCorrelationId())
                .setMessage(event.getResult().toString())
                .build()
        );
    }
}
