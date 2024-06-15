package gc.garcol.bankclustercore;

import lombok.extern.slf4j.Slf4j;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
public class ReplyBufferEventDispatcherImpl implements ReplyBufferEventDispatcher {
    @Override
    public void dispatch(ReplyBufferEvent event) {
        log.debug("[replying]: {}", event);
    }
}
