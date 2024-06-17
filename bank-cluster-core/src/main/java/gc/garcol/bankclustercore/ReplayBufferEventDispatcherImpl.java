package gc.garcol.bankclustercore;

import com.lmax.disruptor.dsl.Disruptor;
import lombok.RequiredArgsConstructor;

/**
 * @author thaivc
 * @since 2024
 */
@RequiredArgsConstructor
public class ReplayBufferEventDispatcherImpl implements ReplayBufferEventDispatcher {
    private final Disruptor<ReplayBufferEvent> replayBufferEventDisruptor;

    @Override
    public void dispatch(ReplayBufferEvent replayBufferEvent) {
        replayBufferEventDisruptor.publishEvent(((event, sequence) -> event.setCommand(replayBufferEvent.getCommand())));
    }
}
