package gc.garcol.bankcluster.transport.rest;

import gc.garcol.bankclustercore.BaseResult;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2024
 */
@Profile("leader")
@RestController("/api/v1/balance-command-sync")
public class BalanceCommandResource {

    @PostMapping("/create-balance")
    public CompletableFuture<BaseResult> createBalance() {
        return null;
    }

}
