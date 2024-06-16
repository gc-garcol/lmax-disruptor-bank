package gc.garcol.bankclustercore;

import com.lmax.disruptor.dsl.Disruptor;
import lombok.RequiredArgsConstructor;

/**
 * @author thaivc
 * @since 2024
 */
@RequiredArgsConstructor
public class CommandBufferEventDispatcherImpl implements CommandBufferEventDispatcher {

    private final Disruptor<CommandBufferEvent> commandBufferEventDisruptor;

    @Override
    public void dispatch(CommandBufferEvent commandBufferEvent) {
        commandBufferEventDisruptor.publishEvent(((event, sequence) -> event.copy(commandBufferEvent)));
    }
}
