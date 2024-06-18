package gc.garcol.bankcluster.transport.grpc;

import gc.garcol.bank.proto.BalanceCommandServiceGrpc;
import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankcluster.infra.SimpleReplier;
import gc.garcol.bankclustercore.BaseCommand;
import gc.garcol.bankclustercore.CommandBufferEvent;
import gc.garcol.bankclustercore.CommandBufferEventDispatcher;
import gc.garcol.bankclustercore.cluster.ClusterStatus;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RequiredArgsConstructor
public class BalanceCommandGrpc extends BalanceCommandServiceGrpc.BalanceCommandServiceImplBase {

    private final CommandBufferEventDispatcher commandBufferEventDispatcher;
    private final SimpleReplier simpleReplier;

    /**
     * Send command to balance service
     */
    @Override
    public StreamObserver<BalanceProto.BalanceCommand> sendCommand(StreamObserver<BalanceProto.BaseResult> responseObserver) {
        var replyChannel = UUID.randomUUID().toString();
        simpleReplier.repliers.put(replyChannel, responseObserver);
        return new StreamObserver<>() {
            @Override
            public void onNext(BalanceProto.BalanceCommand balanceCommand) {
                if (ClusterStatus.STATE.get().equals(ClusterStatus.NOT_AVAILABLE)) {
                    responseObserver.onNext(
                        BalanceProto.BaseResult.newBuilder()
                            .setCode(503)
                            .setMessage("Service not available")
                            .build()
                    );
                }
                try {
                    switch (balanceCommand.getTypeCase()) {
                        case CREATEBALANCECOMMAND -> commandBufferEventDispatcher.dispatch(
                            new CommandBufferEvent(
                                replyChannel,
                                balanceCommand.getCreateBalanceCommand().getCorrelationId(),
                                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                                    .setCreateBalanceCommand(balanceCommand.getCreateBalanceCommand())
                                    .build()
                                )
                            )
                        );
                        case DEPOSITCOMMAND -> commandBufferEventDispatcher.dispatch(
                            new CommandBufferEvent(
                                replyChannel,
                                balanceCommand.getDepositCommand().getCorrelationId(),
                                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                                    .setDepositCommand(balanceCommand.getDepositCommand())
                                    .build()
                                )
                            )
                        );
                        case WITHDRAWCOMMAND -> commandBufferEventDispatcher.dispatch(
                            new CommandBufferEvent(
                                replyChannel,
                                balanceCommand.getWithdrawCommand().getCorrelationId(),
                                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                                    .setWithdrawCommand(balanceCommand.getWithdrawCommand())
                                    .build()
                                )
                            )
                        );
                        case TRANSFERCOMMAND -> commandBufferEventDispatcher.dispatch(
                            new CommandBufferEvent(
                                replyChannel,
                                balanceCommand.getTransferCommand().getCorrelationId(),
                                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                                    .setTransferCommand(balanceCommand.getTransferCommand())
                                    .build()
                                )
                            )
                        );
                        default -> responseObserver.onNext(
                            BalanceProto.BaseResult.newBuilder()
                                .setCode(400)
                                .setMessage("Invalid balance command")
                                .build()
                        );
                    }
                } catch (Exception e) {
                    log.error("Failed to send command", e);
                    responseObserver.onNext(
                        BalanceProto.BaseResult.newBuilder()
                            .setCode(500)
                            .setMessage("Failed to send command")
                            .build()
                    );
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("on command streaming error", throwable);
                simpleReplier.repliers.remove(replyChannel);
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                log.info("on completed command streaming");
                simpleReplier.repliers.remove(replyChannel);
                responseObserver.onCompleted();
            }
        };
    }
}
