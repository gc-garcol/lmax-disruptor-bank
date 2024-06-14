package gc.garcol.bankclustercore.cluster;

import gc.garcol.bankclustercore.StateMachineManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class LeaderBootstrap implements ClusterBootstrap {

    private final StateMachineManager stateMachineManager;

    @Override
    public void onStart() {
        try {
            log.info("Leader start");
            loadingStateMachine();
            activeCommandChannel();
            activeCLuster();
            log.info("Leader started");
        } catch (Exception e) {
            log.error("Leader start failed", e);
            System.exit(-9);
        }
    }

    private void loadingStateMachine() {
        stateMachineManager.loadingStateMachine();
    }

    private void activeCommandChannel() {
        // todo
    }

    private void activeCLuster() {
        ClusterStatus.STATE.set(ClusterStatus.ACTIVE);
    }
}
