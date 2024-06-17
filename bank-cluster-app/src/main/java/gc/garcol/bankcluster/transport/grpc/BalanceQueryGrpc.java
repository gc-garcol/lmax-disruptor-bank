package gc.garcol.bankcluster.transport.grpc;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bank.proto.BalanceQueryServiceGrpc;
import gc.garcol.bankclustercore.account.Balance;
import gc.garcol.bankclustercore.account.Balances;
import gc.garcol.bankclustercore.cluster.ClusterStatus;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RequiredArgsConstructor
public class BalanceQueryGrpc extends BalanceQueryServiceGrpc.BalanceQueryServiceImplBase {

    private final Balances balances;

    @Override
    public StreamObserver<BalanceProto.BalanceQuery> sendQuery(StreamObserver<BalanceProto.BalanceQueryResult> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(BalanceProto.BalanceQuery balanceQuery) {
                if (ClusterStatus.STATE.get().equals(ClusterStatus.NOT_AVAILABLE)) {
                    responseObserver.onNext(
                        BalanceProto.BalanceQueryResult.newBuilder()
                            .setBaseResult(
                                BalanceProto.BaseResult.newBuilder()
                                    .setCode(503)
                                    .build()
                            )
                            .build()
                    );
                    return;
                }
                switch (balanceQuery.getTypeCase()) {
                    case BALANCEFILTERQUERY -> {
                        Balance balance = balances.getBalance(balanceQuery.getBalanceFilterQuery().getId());
                        if (balance != null) {
                            responseObserver.onNext(
                                    BalanceProto.BalanceQueryResult.newBuilder()
                                            .setBalance(BalanceProto.Balance.newBuilder()
                                                    .setId(balance.getId())
                                                    .setAmount(balance.getAmount())
                                                    .setPrecision(balance.getPrecision())
                                                    .setActive(balance.isActive())
                                                    .setCorrelationId(balanceQuery.getBalanceFilterQuery().getCorrelationId())
                                                    .build()
                                            )
                                            .build()
                            );
                        } else {
                            responseObserver.onNext(
                                BalanceProto.BalanceQueryResult.newBuilder()
                                    .setBaseResult(
                                        BalanceProto.BaseResult.newBuilder()
                                            .setCode(404)
                                            .setCorrelationId(balanceQuery.getBalanceFilterQuery().getCorrelationId())
                                            .build()
                                    )
                                    .build()
                            );
                        }
                    }
                    case TYPE_NOT_SET -> {}
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("Error in BalanceQueryGrpc: {}", throwable.getMessage());
                responseObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                log.info("on completed in BalanceQueryGrpc");
                responseObserver.onCompleted();
            }
        };
    }
}
