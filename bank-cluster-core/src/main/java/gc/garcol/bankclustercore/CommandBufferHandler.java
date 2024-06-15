package gc.garcol.bankclustercore;

import com.lmax.disruptor.EventHandler;

/**
 * @author thaivc
 * @since 2024
 */
public interface CommandBufferHandler extends EventHandler<CommandBufferEvent>, CommandBufferChannel {
}
