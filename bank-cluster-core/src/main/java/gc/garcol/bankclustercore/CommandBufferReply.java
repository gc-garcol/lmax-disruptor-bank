package gc.garcol.bankclustercore;

import com.lmax.disruptor.EventHandler;

/**
 * Send to outbound buffers
 *
 * @author thaivc
 * @since 2024
 */
public interface CommandBufferReply extends EventHandler<CommandBufferEvent>, CommandBufferChannel {
}
