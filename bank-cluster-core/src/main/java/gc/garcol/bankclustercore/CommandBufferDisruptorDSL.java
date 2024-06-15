package gc.garcol.bankclustercore;

import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.RequiredArgsConstructor;

/**
 * @author thaivc
 * @since 2024
 */
@RequiredArgsConstructor
public class CommandBufferDisruptorDSL implements DisruptorDSL<CommandBufferEvent> {
    private final CommandBufferJournaler commandBufferJournaler;
    private final CommandBufferHandler commandBufferHandler;
    private final CommandBufferReply commandBufferReply;

    @Override
    public Disruptor<CommandBufferEvent> build(int bufferSize, WaitStrategy waitStrategy) {
        var disruptor = new Disruptor<>(
            CommandBufferEvent::new,
            bufferSize,
            DaemonThreadFactory.INSTANCE,
            ProducerType.SINGLE,
            waitStrategy
        );
        disruptor.handleEventsWith(commandBufferJournaler)
            .then(commandBufferHandler)
            .then(commandBufferReply);
        return disruptor;
    }
}
