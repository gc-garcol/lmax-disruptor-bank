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
public class ReplyBufferDisruptorDSL implements DisruptorDSL<ReplyBufferEvent> {
    private ReplyBufferHandler replyBufferHandler;

    @Override
    public Disruptor<ReplyBufferEvent> build(int bufferSize, WaitStrategy waitStrategy) {
        var disruptor = new Disruptor<>(
            ReplyBufferEvent::new,
            bufferSize,
            DaemonThreadFactory.INSTANCE,
            ProducerType.SINGLE,
            waitStrategy
        );
        disruptor.handleEventsWith(replyBufferHandler);
        return disruptor;
    }
}
