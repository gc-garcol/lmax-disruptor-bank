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
public class BalanceGrpcCommand extends BalanceCommandServiceGrpc.BalanceCommandServiceImplBase {

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
                    responseError(responseObserver, 503, "Service not available", null);
                }
                try {
                    switch (balanceCommand.getTypeCase()) {
                        case CREATEBALANCECOMMAND -> create(replyChannel, balanceCommand);
                        case DEPOSITCOMMAND -> deposit(replyChannel, balanceCommand);
                        case WITHDRAWCOMMAND -> withdraw(replyChannel, balanceCommand);
                        case TRANSFERCOMMAND -> transfer(replyChannel, balanceCommand);
                        default -> responseError(responseObserver, 400, "Invalid command", null);
                    }
                } catch (Exception e) {
                    responseError(responseObserver, 500, "Internal server error", e);
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

    private void create(String replyChannel, BalanceProto.BalanceCommand balanceCommand) {
        commandBufferEventDispatcher.dispatch(
            new CommandBufferEvent(
                replyChannel,
                balanceCommand.getCreateBalanceCommand().getCorrelationId(),
                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                    .setCreateBalanceCommand(balanceCommand.getCreateBalanceCommand())
                    .build()
                )
            )
        );
    }

    private void deposit(String replyChannel, BalanceProto.BalanceCommand balanceCommand) {
        commandBufferEventDispatcher.dispatch(
            new CommandBufferEvent(
                replyChannel,
                balanceCommand.getDepositCommand().getCorrelationId(),
                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                    .setDepositCommand(balanceCommand.getDepositCommand())
                    .build()
                )
            )
        );
    }

    private void withdraw(String replyChannel, BalanceProto.BalanceCommand balanceCommand) {
        commandBufferEventDispatcher.dispatch(
            new CommandBufferEvent(
                replyChannel,
                balanceCommand.getWithdrawCommand().getCorrelationId(),
                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                    .setWithdrawCommand(balanceCommand.getWithdrawCommand())
                    .build()
                )
            )
        );
    }

    private void transfer(String replyChannel, BalanceProto.BalanceCommand balanceCommand) {
        commandBufferEventDispatcher.dispatch(
            new CommandBufferEvent(
                replyChannel,
                balanceCommand.getTransferCommand().getCorrelationId(),
                new BaseCommand(BalanceProto.CommandLog.newBuilder()
                    .setTransferCommand(balanceCommand.getTransferCommand())
                    .build()
                )
            )
        );
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
