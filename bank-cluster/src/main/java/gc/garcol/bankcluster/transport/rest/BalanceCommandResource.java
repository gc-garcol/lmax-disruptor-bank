package gc.garcol.bankcluster.transport.rest;

import gc.garcol.bankclustercore.BaseResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController("/api/v1/balance-command-sync")
public class BalanceCommandResource {

    @PostMapping("/create-balance")
    public CompletableFuture<BaseResponse> createBalance() {
        return null;
    }

}
