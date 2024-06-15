package gc.garcol.bankclustercore;

import lombok.RequiredArgsConstructor;

/**
 * Handle business logic.
 *
 * @author thaivc
 * @since 2024
 */
@RequiredArgsConstructor
public class CommandBufferHandlerImpl implements CommandBufferHandler {

    private final CommandHandler commandHandler;

    @Override
    public void onEvent(CommandBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        event.setResult(commandHandler.onCommand(event.getCommand()));
    }
}
