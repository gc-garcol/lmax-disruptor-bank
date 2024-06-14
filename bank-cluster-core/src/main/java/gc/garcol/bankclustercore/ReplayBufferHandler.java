package gc.garcol.bankclustercore;

import com.lmax.disruptor.EventHandler;

public interface ReplayBufferHandler extends EventHandler<ReplayBufferEvent>, ReplayBufferChanel, CommandHandler {
}
