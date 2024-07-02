package gc.garcol.bankclientcore.cluster;

import reactor.core.publisher.Mono;

public interface ReactorDispatcher<T> {

    Mono<BaseResponse> dispatch(T command);

}
