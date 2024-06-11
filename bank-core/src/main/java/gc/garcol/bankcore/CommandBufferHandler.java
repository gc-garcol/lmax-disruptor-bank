package gc.garcol.bankcore;

import com.lmax.disruptor.EventHandler;

public interface CommandBufferHandler extends EventHandler<CommandBufferEvent>, CommandBufferChannel {
    void onEvent(CommandBufferEvent event);
}
