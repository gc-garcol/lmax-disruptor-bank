package gc.garcol.benchmarkclusterjmh.domain.cluster;

import gc.garcol.bank.proto.BalanceCommandServiceGrpc;
import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.benchmarkclusterjmh.common.BaseResponse;
import gc.garcol.benchmarkclusterjmh.domain.cluster.commands.*;
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

    record Tuple(Runnable callback, CompletableFuture<BaseResponse> future) {}

    private final BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub;

    private volatile StreamObserver<BalanceProto.BalanceCommand> requestStreamObserver;
    private static final Map<String, Tuple> replyFutures = new ConcurrentHashMap<>();

    public BalanceCommandStub(BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub) {
        this.balanceCommandServiceStub = balanceCommandServiceStub;
        initRequestStreamObserver();
    }

    private void initRequestStreamObserver() {
        this.requestStreamObserver = balanceCommandServiceStub.sendCommand(responseObserver());
    }

    public void closeConnection() {
        requestStreamObserver.onCompleted();
    }

    public synchronized CompletableFuture<BaseResponse> sendCommand(BalanceCommand balanceCommand, Runnable callback) {
        return switch (balanceCommand) {
            case CreateBalanceCommand createCommand -> create(createCommand, callback);
            case DepositCommand depositCommand -> deposit(depositCommand, callback);
            case WithdrawCommand withdrawCommand -> withdraw(withdrawCommand, callback);
            case TransferCommand transferCommand -> transfer(transferCommand, callback);
            default -> CompletableFuture.completedFuture(new BaseResponse(404, "Command not found"));
        };
    }

    private CompletableFuture<BaseResponse> create(CreateBalanceCommand createCommand, Runnable callback) {
        var clusterCommand = BalanceProto.CreateBalanceCommand.newBuilder()
            .setCorrelationId(createCommand.getCorrelationId())
            .build();
        var future = new CompletableFuture<BaseResponse>();
        replyFutures.put(createCommand.getCorrelationId(), new Tuple(callback, future));
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setCreateBalanceCommand(clusterCommand).build());
        return future;
    }

    private CompletableFuture<BaseResponse> deposit(DepositCommand depositCommand, Runnable callback) {
        var clusterCommand = BalanceProto.DepositCommand.newBuilder()
            .setCorrelationId(depositCommand.getCorrelationId())
            .setId(depositCommand.getId())
            .setAmount(depositCommand.getAmount())
            .build();
        var future = new CompletableFuture<BaseResponse>();
        replyFutures.put(depositCommand.getCorrelationId(), new Tuple(callback, future));
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setDepositCommand(clusterCommand).build());
        return future;
    }

    private CompletableFuture<BaseResponse> withdraw(WithdrawCommand withdrawCommand, Runnable callback) {
        var clusterCommand = BalanceProto.WithdrawCommand.newBuilder()
            .setCorrelationId(withdrawCommand.getCorrelationId())
            .setId(withdrawCommand.getId())
            .setAmount(withdrawCommand.getAmount())
            .build();
        var future = new CompletableFuture<BaseResponse>();
        replyFutures.put(withdrawCommand.getCorrelationId(), new Tuple(callback, future));
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setWithdrawCommand(clusterCommand).build());
        return future;
    }

    private CompletableFuture<BaseResponse> transfer(TransferCommand transferCommand, Runnable callback) {
        var clusterCommand = BalanceProto.TransferCommand.newBuilder()
            .setCorrelationId(transferCommand.getCorrelationId())
            .setFromId(transferCommand.getFromId())
            .setToId(transferCommand.getToId())
            .setAmount(transferCommand.getAmount())
            .build();
        var future = new CompletableFuture<BaseResponse>();
        replyFutures.put(transferCommand.getCorrelationId(), new Tuple(callback, future));
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setTransferCommand(clusterCommand).build());
        return future;
    }

    private StreamObserver<BalanceProto.BaseResult> responseObserver() {
        return new StreamObserver<>() {
            @Override
            public void onNext(BalanceProto.BaseResult baseResult) {
                Optional.ofNullable(replyFutures.remove(baseResult.getCorrelationId()))
                    .ifPresent(tuple -> {
                        tuple.future.complete(new BaseResponse(baseResult.getCode(), baseResult.getMessage()));
                        tuple.callback.run();
                    });
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
