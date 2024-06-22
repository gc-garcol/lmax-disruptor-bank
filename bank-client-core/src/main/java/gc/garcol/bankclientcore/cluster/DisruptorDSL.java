package gc.garcol.bankclientcore.cluster;

import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * @author thaivc
 * @since 2024
 */
public interface DisruptorDSL<T> {
    Disruptor<T> build(int bufferSize, WaitStrategy waitStrategy);
}
