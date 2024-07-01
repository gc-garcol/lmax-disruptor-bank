package gc.garcol.bankcluster.transport.grpc;

import gc.garcol.bank.proto.BalanceCommandServiceGrpc;
import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankcluster.infra.SimpleReplier;
import gc.garcol.bankcluster.transport.CommandBufferEventDispatcherWrapper;
import gc.garcol.bankclustercore.cluster.ClusterStatus;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RequiredArgsConstructor
public class BalanceCommandGrpc extends BalanceCommandServiceGrpc.BalanceCommandServiceImplBase {

    private final CommandBufferEventDispatcherWrapper commandBufferEventDispatcherWrapper;
    private final SimpleReplier simpleReplier;

    @PreDestroy
    void destroy() {
        simpleReplier.grpcRepliers.clear();
    }

    /**
     * Send command to balance service
     */
    @Override
    public StreamObserver<BalanceProto.BalanceCommand> sendCommand(StreamObserver<BalanceProto.BaseResult> responseObserver) {
        var replyChannel = String.format("grpc-%s", ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE));
        simpleReplier.grpcRepliers.put(replyChannel, responseObserver);
        return new StreamObserver<>() {
            @Override
            public void onNext(BalanceProto.BalanceCommand balanceCommand) {
                if (ClusterStatus.STATE.get().equals(ClusterStatus.NOT_AVAILABLE)) {
                    responseError(responseObserver, 503, "Service not available", null);
                }
                try {
                    switch (balanceCommand.getTypeCase()) {
                        case CREATEBALANCECOMMAND -> commandBufferEventDispatcherWrapper.create(replyChannel, balanceCommand);
                        case DEPOSITCOMMAND -> commandBufferEventDispatcherWrapper.deposit(replyChannel, balanceCommand);
                        case WITHDRAWCOMMAND -> commandBufferEventDispatcherWrapper.withdraw(replyChannel, balanceCommand);
                        case TRANSFERCOMMAND -> commandBufferEventDispatcherWrapper.transfer(replyChannel, balanceCommand);
                        default -> responseError(responseObserver, 400, "Invalid command", null);
                    }
                } catch (Exception e) {
                    responseError(responseObserver, 500, "Internal server error", e);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("on command streaming error", throwable);
                simpleReplier.grpcRepliers.remove(replyChannel);
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                log.info("on completed command streaming");
                simpleReplier.grpcRepliers.remove(replyChannel);
                responseObserver.onCompleted();
            }
        };
    }

    private void responseError(StreamObserver<BalanceProto.BaseResult> responseObserver, int code, String message, Exception e) {
        if (e != null) {
            log.error(message, e);
        }
        responseObserver.onNext(
            BalanceProto.BaseResult.newBuilder()
                .setCode(code)
                .setMessage(message)
                .build()
        );
    }
}
