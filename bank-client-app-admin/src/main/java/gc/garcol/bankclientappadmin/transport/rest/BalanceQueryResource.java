package gc.garcol.bankclientappadmin.transport.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RequestMapping("/api/v1/balance/query")
@RestController
public class BalanceQueryResource {

    @PostMapping("/current-balance")
    public CompletableFuture<BaseResponse> currentBalance() {
        return CompletableFuture.supplyAsync(() -> new BaseResponse(200, "Success"));
    }
}
