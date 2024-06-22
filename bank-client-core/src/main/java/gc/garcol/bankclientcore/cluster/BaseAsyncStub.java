package gc.garcol.bankclientcore.cluster;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.agrona.collections.Object2ObjectHashMap;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
public abstract class BaseAsyncStub<REQUEST, RESULT> {

    protected StreamObserver<REQUEST> requestStreamObserver;

    protected final Map<String, CompletableFuture<BaseResponse>> replyFutures = new Object2ObjectHashMap<>();

    protected void initRequestStreamObserver() {
        requestStreamObserver = initRequestStreamObserver(responseObserver());
    }
    protected abstract StreamObserver<REQUEST> initRequestStreamObserver(StreamObserver<RESULT> responseObserver);

    protected abstract void sendGrpcMessage(RequestBufferEvent request);

    protected abstract String extractResultCorrelationId(RESULT result);
    protected abstract BaseResponse extractResult(RESULT result);

    protected StreamObserver<RESULT> responseObserver() {
        return new StreamObserver<>() {
            @Override
            public void onNext(RESULT result) {
                Optional.ofNullable(replyFutures.remove(extractResultCorrelationId(result)))
                    .ifPresent(future -> future.complete(extractResult(result)));
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("on error observer balance", throwable);
                replyFutures.clear();
                silentSleep(5_000);
                initRequestStreamObserver();
            }

            @Override
            public void onCompleted() {
                log.info("on completed observer {}", this.getClass().getSimpleName());
                replyFutures.clear();
            }
        };
    }

    private void silentSleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.error("Something get wrong when sleep", e);
        }
    }

}
