package gc.garcol.bankclientappadmin.infra;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bank.proto.BalanceQueryServiceGrpc;
import gc.garcol.bankclientappadmin.infra.query.BalanceDetailQuery;
import gc.garcol.bankclientappadmin.infra.response.BalanceDetailResponse;
import gc.garcol.bankclientcore.cluster.BaseAsyncStub;
import gc.garcol.bankclientcore.cluster.BaseResponse;
import gc.garcol.bankclientcore.cluster.RequestBufferEvent;
import io.grpc.stub.StreamObserver;

/**
 * @author thaivc
 * @since 2024
 */
public class BalanceQueryStub extends BaseAsyncStub<BalanceProto.BalanceQuery, BalanceProto.BalanceQueryResult> {

    private final BalanceQueryServiceGrpc.BalanceQueryServiceStub balanceQueryServiceStub;

    public BalanceQueryStub(BalanceQueryServiceGrpc.BalanceQueryServiceStub balanceQueryServiceStub) {
        this.balanceQueryServiceStub = balanceQueryServiceStub;
        this.initRequestStreamObserver();
    }

    @Override
    protected StreamObserver<BalanceProto.BalanceQuery> initRequestStreamObserver(StreamObserver<BalanceProto.BalanceQueryResult> responseObserver) {
        return balanceQueryServiceStub.sendQuery(responseObserver);
    }

    @Override
    protected void sendGrpcMessage(RequestBufferEvent request) {
        try {
            replyFutures.put(request.getRequest().getCorrelationId(), request.getResponseFuture());
            switch (request.getRequest()) {
                case BalanceDetailQuery balanceDetailQuery -> balanceDetail(balanceDetailQuery);
                default -> replyFutures.remove(request.getRequest().getCorrelationId());
            }
        } catch (Exception e) {
            replyFutures.remove(request.getRequest().getCorrelationId());
            throw e;
        }
    }

    @Override
    protected String extractResultCorrelationId(BalanceProto.BalanceQueryResult balanceQueryResult) {
        return switch (balanceQueryResult.getTypeCase()) {
            case BALANCE -> balanceQueryResult.getBalance().getCorrelationId();
            default -> balanceQueryResult.getBaseResult().getCorrelationId();
        };
    }

    @Override
    protected BaseResponse extractResult(BalanceProto.BalanceQueryResult balanceQueryResult) {
        return switch (balanceQueryResult.getTypeCase()) {
            case BALANCE -> new BalanceDetailResponse(balanceQueryResult.getBalance().getId(), balanceQueryResult.getBalance().getAmount());
            case BASERESULT -> new BaseResponse(balanceQueryResult.getBaseResult().getCode(), balanceQueryResult.getBaseResult().getMessage());
            default -> new BaseResponse(500, "Unknown response!!!");
        };
    }

    private void balanceDetail(BalanceDetailQuery balanceDetailQuery) {
        var balanceDetail = BalanceProto.BalanceFilterQuery.newBuilder()
            .setCorrelationId(balanceDetailQuery.getCorrelationId())
            .setId(balanceDetailQuery.getId())
            .build();
        requestStreamObserver.onNext(BalanceProto.BalanceQuery.newBuilder()
            .setBalanceFilterQuery(balanceDetail)
            .build());
    }
}
