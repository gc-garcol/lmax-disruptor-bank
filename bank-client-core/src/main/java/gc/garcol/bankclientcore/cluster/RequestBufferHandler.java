package gc.garcol.bankclientcore.cluster;

import com.lmax.disruptor.EventHandler;

/**
 * @author thaivc
 * @since 2024
 */
public interface RequestBufferHandler extends EventHandler<RequestBufferEvent>, RequestBufferChannel {
}
