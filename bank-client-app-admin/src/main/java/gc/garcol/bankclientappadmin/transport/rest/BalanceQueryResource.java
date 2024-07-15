package gc.garcol.bankclientappadmin.transport.rest;

import gc.garcol.bankclientappadmin.common.query.BalanceDetailQuery;
import gc.garcol.bankclientcore.cluster.BaseRequest;
import gc.garcol.bankclientcore.cluster.BaseResponse;
import gc.garcol.bankclientcore.cluster.RequestBufferDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/balance/query")
@RequiredArgsConstructor
public class BalanceQueryResource {

    private final RequestBufferDispatcher<BaseRequest> requestBufferDispatcher;

    @GetMapping
    public CompletableFuture<BaseResponse> currentBalance(BalanceDetailQuery balanceDetailQuery) {
        return requestBufferDispatcher.dispatch(balanceDetailQuery);
    }
}
