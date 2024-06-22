package gc.garcol.bankclientcore.cluster;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2024
 */
public interface RequestBufferDispatcher<T> extends RequestBufferChannel {
    CompletableFuture<BaseResponse> dispatch(T request);
}
