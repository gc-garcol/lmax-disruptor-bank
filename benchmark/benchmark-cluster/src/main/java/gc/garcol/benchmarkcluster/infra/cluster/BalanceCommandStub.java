package gc.garcol.benchmarkcluster.infra.cluster;

import gc.garcol.bank.proto.BalanceCommandServiceGrpc;
import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.benchmarkcluster.common.BaseReply;
import gc.garcol.benchmarkcluster.common.commands.*;
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

    private StreamObserver<BalanceProto.BalanceCommand> requestStreamObserver;
    public final Map<String, BaseReply> replyFutures = new ConcurrentHashMap<>();

    public BalanceCommandStub(BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub) {
        this.balanceCommandServiceStub = balanceCommandServiceStub;
        initRequestStreamObserver();
    }

    private void initRequestStreamObserver() {
        this.requestStreamObserver = balanceCommandServiceStub.sendCommand(responseObserver());
    }

    public CompletableFuture<Long> sendCommand(BalanceCommand balanceCommand) {
        return switch (balanceCommand) {
            case CreateBalanceCommand createCommand -> create(createCommand);
            case DepositCommand depositCommand -> deposit(depositCommand);
            case WithdrawCommand withdrawCommand -> withdraw(withdrawCommand);
            case TransferCommand transferCommand -> transfer(transferCommand);
            default -> CompletableFuture.completedFuture(-1L);
        };
    }

    private CompletableFuture<Long> create(CreateBalanceCommand createCommand) {
        var clusterCommand = BalanceProto.CreateBalanceCommand.newBuilder()
            .setCorrelationId(createCommand.getCorrelationId())
            .build();
        var reply = new BaseReply(System.nanoTime());
        replyFutures.put(createCommand.getCorrelationId(), reply);
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setCreateBalanceCommand(clusterCommand).build());
        return reply.getFuture();
    }

    private CompletableFuture<Long> deposit(DepositCommand depositCommand) {
        var clusterCommand = BalanceProto.DepositCommand.newBuilder()
            .setCorrelationId(depositCommand.getCorrelationId())
            .setId(depositCommand.getId())
            .setAmount(depositCommand.getAmount())
            .build();
        var reply = new BaseReply(System.nanoTime());
        replyFutures.put(depositCommand.getCorrelationId(), reply);
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setDepositCommand(clusterCommand).build());
        return reply.getFuture();
    }

    private CompletableFuture<Long> withdraw(WithdrawCommand withdrawCommand) {
        var clusterCommand = BalanceProto.WithdrawCommand.newBuilder()
            .setCorrelationId(withdrawCommand.getCorrelationId())
            .setId(withdrawCommand.getId())
            .setAmount(withdrawCommand.getAmount())
            .build();
        var reply = new BaseReply(System.nanoTime());
        replyFutures.put(withdrawCommand.getCorrelationId(), reply);
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setWithdrawCommand(clusterCommand).build());
        return reply.getFuture();
    }

    private CompletableFuture<Long> transfer(TransferCommand transferCommand) {
        var clusterCommand = BalanceProto.TransferCommand.newBuilder()
            .setCorrelationId(transferCommand.getCorrelationId())
            .setFromId(transferCommand.getFromId())
            .setToId(transferCommand.getToId())
            .setAmount(transferCommand.getAmount())
            .build();
        var reply = new BaseReply(System.nanoTime());
        replyFutures.put(transferCommand.getCorrelationId(), reply);
        requestStreamObserver.onNext(BalanceProto.BalanceCommand.newBuilder().setTransferCommand(clusterCommand).build());
        return reply.getFuture();
    }

    private StreamObserver<BalanceProto.BaseResult> responseObserver() {
        return new StreamObserver<>() {
            @Override
            public void onNext(BalanceProto.BaseResult baseResult) {
//                Optional.ofNullable(replyFutures.remove(baseResult.getCorrelationId()))
                Optional.ofNullable(replyFutures.get(baseResult.getCorrelationId()))
                    .ifPresent(reply -> {
                        reply.setEndTime(System.nanoTime());
                        reply.getFuture().complete(reply.getEndTime() - reply.getStartTime());
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
