package gc.garcol.benchmarkcluster.domain.cluster;

import gc.garcol.bank.proto.BalanceCommandServiceGrpc;
import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.benchmarkcluster.domain.cluster.commands.*;
import gc.garcol.benchmarkcluster.transport.rest.BaseResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
public class BalanceCommandStub {

    private final BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub;

    private volatile StreamObserver<BalanceProto.BalanceCommand> requestStreamObserver;
    private static final Map<String, CompletableFuture<BaseResponse>> replyFutures = new ConcurrentHashMap<>();

    public BalanceCommandStub(BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub) {
        this.balanceCommandServiceStub = balanceCommandServiceStub;
        initRequestStreamObserver();
    }

    private void initRequestStreamObserver() {
        this.requestStreamObserver = balanceCommandServiceStub.sendCommand(responseObserver());
    }

    public CompletableFuture<BaseResponse> sendCommand(BalanceCommand balanceCommand) {
        return switch (balanceCommand) {
            case CreateBalanceCommand createCommand -> create(createCommand);
            case DepositCommand depositCommand -> deposit(depositCommand);
            case WithdrawCommand withdrawCommand -> withdraw(withdrawCommand);
            case TransferCommand transferCommand -> transfer(transferCommand);
            default -> CompletableFuture.completedFuture(new BaseResponse(404, "Command not found"));
        };
    }

    private CompletableFuture<BaseResponse> create(CreateBalanceCommand createCommand) {
        var clusterCommand = BalanceProto.CreateBalanceCommand.newBuilder()
            .setCorrelationId(createCommand.getCorrelationId())
            .build();
        var future = new CompletableFuture<BaseResponse>();
        replyFutures.put(createCommand.getCorrelationId(), future);
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setCreateBalanceCommand(clusterCommand).build());
        return future;
    }

    private CompletableFuture<BaseResponse> deposit(DepositCommand depositCommand) {
        var clusterCommand = BalanceProto.DepositCommand.newBuilder()
            .setCorrelationId(depositCommand.getCorrelationId())
            .setId(depositCommand.getId())
            .setAmount(depositCommand.getAmount())
            .build();
        var future = new CompletableFuture<BaseResponse>();
        replyFutures.put(depositCommand.getCorrelationId(), future);
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setDepositCommand(clusterCommand).build());
        return future;
    }

    private CompletableFuture<BaseResponse> withdraw(WithdrawCommand withdrawCommand) {
        var clusterCommand = BalanceProto.WithdrawCommand.newBuilder()
            .setCorrelationId(withdrawCommand.getCorrelationId())
            .setId(withdrawCommand.getId())
            .setAmount(withdrawCommand.getAmount())
            .build();
        var future = new CompletableFuture<BaseResponse>();
        replyFutures.put(withdrawCommand.getCorrelationId(), future);
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setWithdrawCommand(clusterCommand).build());
        return future;
    }

    private CompletableFuture<BaseResponse> transfer(TransferCommand transferCommand) {
        var clusterCommand = BalanceProto.TransferCommand.newBuilder()
            .setCorrelationId(transferCommand.getCorrelationId())
            .setFromId(transferCommand.getFromId())
            .setToId(transferCommand.getToId())
            .setAmount(transferCommand.getAmount())
            .build();
        var future = new CompletableFuture<BaseResponse>();
        replyFutures.put(transferCommand.getCorrelationId(), future);
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setTransferCommand(clusterCommand).build());
        return future;
    }

    private StreamObserver<BalanceProto.BaseResult> responseObserver() {
        return new StreamObserver<>() {
            @Override
            public void onNext(BalanceProto.BaseResult baseResult) {
                Optional.ofNullable(replyFutures.remove(baseResult.getCorrelationId()))
                    .ifPresent(future -> future.complete(new BaseResponse(baseResult.getCode(), baseResult.getMessage())));
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("on error observer balance", throwable);
                replyFutures.clear();
                silentSleep(2_000);
                initRequestStreamObserver();
            }

            @Override
            public void onCompleted() {
                log.info("on completed observer balance");
                replyFutures.clear();
            }
        };
    }

    private void silentSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {}
    }

}
