package gc.garcol.bankclientappadmin.infra;

import gc.garcol.bankclientappadmin.infra.command.BalanceCommand;
import gc.garcol.bankclientappadmin.infra.query.BalanceQuery;
import gc.garcol.bankclientcore.cluster.BaseResponse;
import gc.garcol.bankclientcore.cluster.RequestBufferEvent;
import gc.garcol.bankclientcore.cluster.RequestBufferHandler;
import gc.garcol.common.exception.Bank4xxException;
import gc.garcol.common.exception.Bank5xxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RequiredArgsConstructor
public class BalanceRequestBufferHandler
    implements RequestBufferHandler {

    private final BalanceCommandStub balanceCommandStub;
    private final BalanceQueryStub balanceQueryStub;

    @Override
    public void onEvent(RequestBufferEvent event, long sequence, boolean endOfBatch) throws Exception {
        try {
            if (event.getRequest() instanceof BalanceCommand) {
                balanceCommandStub.sendGrpcMessage(event);
            } else if (event.getRequest() instanceof BalanceQuery) {
                balanceQueryStub.sendGrpcMessage(event);
            } else {
                log.error("Unknown event type: {}", event.getClass().getName());
                event.getResponseFuture().complete(new BaseResponse(400, "Unknown event type"));
            }

        } catch (Bank4xxException e) {
            log.error("Error while sending message", e);
            event.getResponseFuture().complete(new BaseResponse(400, e.getMessage()));
        } catch (Bank5xxException e) {
            log.error("Error while sending message", e);
            event.getResponseFuture().complete(new BaseResponse(500, e.getMessage()));
        } catch (Exception e) {
            log.error("Error while sending message", e);
            event.getResponseFuture().complete(new BaseResponse(500, "Internal Server Error"));
        }
    }
}
