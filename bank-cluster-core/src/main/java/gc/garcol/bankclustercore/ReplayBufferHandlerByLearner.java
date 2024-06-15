package gc.garcol.bankclustercore;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;

/**
 * @author thaivc
 * @since 2024
 */
@RequiredArgsConstructor
public class ReplayBufferHandlerByLearner implements ReplayBufferHandler {
    private static final Duration SNAPSHOT_INTERVAL = Duration.ofMinutes(5);
    private static final int SNAPSHOT_SIZE = 10_000;

    private final CommandHandler commandHandler;
    private final StateMachineManager stateMachineManager;

    private int eventCount = SNAPSHOT_SIZE;
    private Instant lastSnapshot = Instant.now();

    @Override
    public void onEvent(ReplayBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        commandHandler.onCommand(event.getCommand());
        eventCount--;
        if (endOfBatch && shouldSnapshot()) {
            stateMachineManager.takeSnapshot();
            resetAfterSnapshot();
        }
    }

    private boolean shouldSnapshot() {
        return eventCount < 0 || Instant.now().compareTo(lastSnapshot.plus(SNAPSHOT_INTERVAL)) > 0;
    }

    private void resetAfterSnapshot() {
        eventCount = SNAPSHOT_SIZE;
        lastSnapshot = Instant.now();
    }
}
