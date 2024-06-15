package gc.garcol.bankclustercore;

import lombok.RequiredArgsConstructor;

/**
 * @author thaivc
 * @since 2024
 */
@RequiredArgsConstructor
public class ReplayBufferHandlerByFollower implements ReplayBufferHandler {

    private final CommandHandler commandHandler;

    @Override
    public void onEvent(ReplayBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        commandHandler.onCommand(event.getCommand());
    }
}
