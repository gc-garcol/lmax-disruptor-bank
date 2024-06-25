package gc.garcol.bankclustercore;

import com.lmax.disruptor.dsl.Disruptor;
import lombok.RequiredArgsConstructor;

/**
 * @author thaivc
 * @since 2024
 */
@RequiredArgsConstructor
public class ReplyBufferEventDispatcherImpl implements ReplyBufferEventDispatcher {

    private final Disruptor<ReplyBufferEvent> replyBufferEventDisruptor;

    @Override
    public void dispatch(ReplyBufferEvent replyBufferEvent) {
        replyBufferEventDisruptor.publishEvent((event, sequence) -> {});
    }
}
