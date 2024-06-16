package gc.garcol.bankclustercore.cluster;

import com.lmax.disruptor.dsl.Disruptor;
import gc.garcol.bankclustercore.ReplayBufferEvent;
import gc.garcol.bankclustercore.StateMachineManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author thaivc
 * @since 2024
 */
@SuppressWarnings("DuplicatedCode")
@Slf4j
@RequiredArgsConstructor
public class LearnerBootstrap implements ClusterBootstrap {
    private final StateMachineManager stateMachineManager;
    private final Disruptor<ReplayBufferEvent> replayBufferEventDisruptor;

    @Override
    public void onStart() {
        try {
            log.info("Learner start");
            loadingStateMachine();
            activeReplayChannel();
            activeCLuster();
            log.info("Learner started");
        } catch (Exception e) {
            log.error("Learner start failed", e);
            System.exit(-9);
        }
    }

    @Override
    public void onStop() {
        replayBufferEventDisruptor.shutdown();
    }

    private void loadingStateMachine() {
        stateMachineManager.loadingStateMachine();
    }

    private void activeReplayChannel() {
        log.info("On starting command-buffer channel");
        replayBufferEventDisruptor.start();
        log.info("On started command-buffer channel");
    }

    private void activeCLuster() {
        ClusterStatus.STATE.set(ClusterStatus.ACTIVE);
    }
}
