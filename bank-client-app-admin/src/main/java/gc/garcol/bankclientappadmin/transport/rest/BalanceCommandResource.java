package gc.garcol.bankclientappadmin.transport.rest;

import gc.garcol.bankclientappadmin.transport.rest.payload.CreateBalanceCommand;
import gc.garcol.bankclientappadmin.transport.rest.payload.DepositCommand;
import gc.garcol.bankclientappadmin.transport.rest.payload.TransferCommand;
import gc.garcol.bankclientappadmin.transport.rest.payload.WithdrawCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/balance/command")
public class BalanceCommandResource {

    @PostMapping("/create")
    public CompletableFuture<BaseResponse> create() {
        var command = new CreateBalanceCommand();
        log.info("Create balance command: {}", command);
        return CompletableFuture.supplyAsync(() -> new BaseResponse(200, "Success"));
    }

    @PostMapping("/deposit")
    public CompletableFuture<BaseResponse> deposit(@RequestBody DepositCommand command) {
        log.info("Deposit command: {}", command);
        return CompletableFuture.supplyAsync(() -> new BaseResponse(200, "Success"));
    }

    @PostMapping("/withdraw")
    public CompletableFuture<BaseResponse> withdraw(@RequestBody WithdrawCommand command) {
        log.info("Withdraw command: {}", command);
        return CompletableFuture.supplyAsync(() -> new BaseResponse(200, "Success"));
    }

    @PostMapping("/transfer")
    public CompletableFuture<BaseResponse> transfer(@RequestBody TransferCommand command) {
        log.info("Transfer command: {}", command);
        return CompletableFuture.supplyAsync(() -> new BaseResponse(200, "Success"));
    }
}
