package gc.garcol.bankclientcore.cluster;

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
public class RequestBufferDisruptorDSL implements DisruptorDSL<RequestBufferEvent> {

    private final RequestBufferHandler requestBufferHandler;

    @Override
    public Disruptor<RequestBufferEvent> build(int bufferSize, WaitStrategy waitStrategy) {
        var disruptor = new Disruptor<>(
            RequestBufferEvent::new,
            bufferSize,
            DaemonThreadFactory.INSTANCE,
            ProducerType.MULTI,
            waitStrategy
        );
        disruptor.handleEventsWith(requestBufferHandler);
        return disruptor;
    }
}
