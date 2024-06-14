package gc.garcol.bankclustercore;

import com.lmax.disruptor.EventHandler;

public interface CommandBufferHandler extends EventHandler<CommandBufferEvent>, CommandHandler, CommandBufferChannel {
    void onEvent(CommandBufferEvent event);
}
