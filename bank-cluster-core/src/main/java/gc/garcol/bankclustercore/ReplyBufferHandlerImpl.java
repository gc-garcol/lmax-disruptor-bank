package gc.garcol.bankclustercore;

import lombok.extern.slf4j.Slf4j;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
public class ReplyBufferHandlerImpl implements ReplyBufferHandler {
    @Override
    public void onEvent(ReplyBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        log.info("Replying event {}", event);
    }
}
