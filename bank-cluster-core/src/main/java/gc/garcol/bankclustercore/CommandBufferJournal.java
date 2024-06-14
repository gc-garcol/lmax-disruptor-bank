package gc.garcol.bankclustercore;

import com.lmax.disruptor.EventHandler;

import java.util.List;

public interface CommandBufferJournal extends EventHandler<CommandBufferEvent>, CommandBufferChannel {
    void journal(List<CommandBufferEvent> events);
}
