package gc.garcol.bankclientappadmin.infra;

import gc.garcol.bank.proto.BalanceProto;
import gc.garcol.bankclientcore.cluster.BaseRequest;
import gc.garcol.bankclientcore.cluster.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReactorBalanceClusterRequester {

    private final RSocketRequester.Builder rsocketRequesterBuilder;

    @Bean
    Sinks.Many<BaseRequest> requestSinks() {
        return Sinks.many().unicast().onBackpressureBuffer();
    }

    @Bean
    Map<String, Sinks.One<BaseResponse>> replySinks() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    RSocketRequester rSocketRequester(Map<String, Sinks.One<BaseResponse>> replySinks) {
        RSocketRequester rsocketRequester = rsocketRequesterBuilder
                .setupRoute("balance-command-channel")
                .tcp("localhost", 7000);

        rsocketRequester.route("balance-command-channel")
                .data(requestSinks().asFlux())
                .retrieveFlux(BalanceProto.BaseResult.class)
                .subscribe(response ->
                    Optional.ofNullable(replySinks.remove(response.getCorrelationId()))
                            .ifPresent(replySink -> replySink.tryEmitValue(
                                    new BaseResponse(response.getCode(), response.getMessage()))
                            )
                );
        return rsocketRequester;
    }

}
