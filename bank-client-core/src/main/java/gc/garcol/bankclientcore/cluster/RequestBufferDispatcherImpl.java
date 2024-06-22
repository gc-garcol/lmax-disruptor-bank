package gc.garcol.bankclientcore.cluster;

import com.lmax.disruptor.dsl.Disruptor;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2024
 */
@RequiredArgsConstructor
public class RequestBufferDispatcherImpl<T extends BaseRequest> implements RequestBufferDispatcher<T> {

    private final Disruptor<RequestBufferEvent> requestBufferEventDisruptor;

    @Override
    public CompletableFuture<BaseResponse> dispatch(T request) {
        CompletableFuture<BaseResponse> responseFuture = new CompletableFuture<>();
        requestBufferEventDisruptor.publishEvent(((event, sequence) -> {
            event.setRequest(request);
            event.setResponseFuture(responseFuture);
        }));
        return responseFuture;
    }
}
