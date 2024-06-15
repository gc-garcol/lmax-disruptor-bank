package gc.garcol.bankclustercore.cluster;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import gc.garcol.bankclustercore.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RequiredArgsConstructor
public class LeaderBootstrap implements ClusterBootstrap {

    private final StateMachineManager stateMachineManager;

    private final CommandBufferDisruptorDSL commandBufferDisruptorDSL;
    private final ReplyBufferDisruptorDSL replyBufferDisruptorDSL;
    private final LeaderProperties leaderProperties;

    private Disruptor<CommandBufferEvent> commandBufferDisruptor;
    private Disruptor<ReplyBufferEvent> replyBufferEventDisruptor;

    @Override
    public void onStart() {
        try {
            log.info("Leader start");
            loadingStateMachine();
            activeReplyChannel();
            activeCommandChannel();
            activeCLuster();
            log.info("Leader started");
        } catch (Exception e) {
            log.error("Leader start failed", e);
            System.exit(-9);
        }
    }

    @Override
    public void onStop() {
        commandBufferDisruptor.shutdown();
    }

    private void loadingStateMachine() {
        stateMachineManager.loadingStateMachine();
    }

    private void activeReplyChannel() {
        log.info("On starting reply-buffer channel");
        replyBufferEventDisruptor = replyBufferDisruptorDSL.build(
            leaderProperties.getReplyBufferSize(),
            new YieldingWaitStrategy()
        );
        replyBufferEventDisruptor.start();
        log.info("On started reply-buffer channel");
    }

    private void activeCommandChannel() {
        log.info("On starting command-buffer channel");
        commandBufferDisruptor = commandBufferDisruptorDSL.build(
            leaderProperties.getCommandBufferSize(),
            new YieldingWaitStrategy()
        );
        commandBufferDisruptor.start();
        log.info("On started command-buffer channel");
    }

    private void activeCLuster() {
        ClusterStatus.STATE.set(ClusterStatus.ACTIVE);
    }
}
