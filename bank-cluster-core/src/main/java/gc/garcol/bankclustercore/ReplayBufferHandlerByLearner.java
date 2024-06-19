package gc.garcol.bankclustercore;

import gc.garcol.bankclustercore.cluster.LearnerProperties;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * @author thaivc
 * @since 2024
 */
@RequiredArgsConstructor
public class ReplayBufferHandlerByLearner implements ReplayBufferHandler {
    private final CommandHandler commandHandler;
    private final StateMachineManager stateMachineManager;
    private final LearnerProperties learnerProperties;

    @Setter
    private int eventCount;
    private Instant lastSnapshot = Instant.now();

    @Override
    public void onEvent(ReplayBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        if (!(event.getCommand() instanceof BaseCommandSnapshot)) {
            commandHandler.onCommand(event.getCommand());
        }
        eventCount--;
        if (endOfBatch && shouldSnapshot()) {
            stateMachineManager.takeSnapshot();
            resetAfterSnapshot();
        }
    }

    private boolean shouldSnapshot() {
        return eventCount < 0 || Instant.now().compareTo(lastSnapshot.plus(learnerProperties.getSnapshotLifeTime())) > 0;
    }

    private void resetAfterSnapshot() {
        eventCount = learnerProperties.getSnapshotFragmentSize();
        lastSnapshot = Instant.now();
    }
}
