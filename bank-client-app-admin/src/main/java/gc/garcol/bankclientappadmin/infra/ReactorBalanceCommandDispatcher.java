package gc.garcol.bankclientappadmin.infra;

import gc.garcol.bankclientappadmin.infra.command.BalanceCommand;
import gc.garcol.bankclientcore.cluster.BaseRequest;
import gc.garcol.bankclientcore.cluster.BaseResponse;
import gc.garcol.bankclientcore.cluster.ReactorDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReactorBalanceCommandDispatcher<T extends BalanceCommand> implements ReactorDispatcher<T> {

    private final Sinks.Many<BaseRequest> requestSinks;
    private final Map<String, Sinks.One<BaseResponse>> replySinks;

    @Override
    public Mono<BaseResponse> dispatch(T command) {
        Sinks.One<BaseResponse> reply = Sinks.one();
        Mono<BaseResponse> response = reply.asMono();
        // todo timeout for response
        // todo remove reply sink after timeout

        requestSinks.tryEmitNext(command);
        replySinks.put(command.getCorrelationId(), reply);
        return response;
    }
}
