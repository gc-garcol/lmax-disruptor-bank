package gc.garcol.bankclientappadmin.transport.rest;

import gc.garcol.bankclientappadmin.infra.command.*;
import gc.garcol.bankclientcore.cluster.BaseRequest;
import gc.garcol.bankclientcore.cluster.BaseResponse;
import gc.garcol.bankclientcore.cluster.ReactorDispatcher;
import gc.garcol.bankclientcore.cluster.RequestBufferDispatcher;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/api/v2/balance/command")
@RequiredArgsConstructor
public class BalanceCommandReactiveResource {

    private final ReactorDispatcher<BalanceCommand> balanceReactiveDispatcher;

    @PostMapping("/create")
    public CompletableFuture<BaseResponse> create() {
        var command = new CreateBalanceCommand();
        log.info("Create balance command: {}", command);
        return requestBufferDispatcher.dispatch(command);
    }

    @PostMapping("/deposit")
    public CompletableFuture<BaseResponse> deposit(@RequestBody DepositCommand command) {
        log.info("Deposit command: {}", command);
        return requestBufferDispatcher.dispatch(command);
    }

    @PostMapping("/withdraw")
    public CompletableFuture<BaseResponse> withdraw(@RequestBody WithdrawCommand command) {
        log.info("Withdraw command: {}", command);
        return requestBufferDispatcher.dispatch(command);
    }

    @PostMapping("/transfer")
    public CompletableFuture<BaseResponse> transfer(@RequestBody TransferCommand command) {
        log.info("Transfer command: {}", command);
        return requestBufferDispatcher.dispatch(command);
    }
}
