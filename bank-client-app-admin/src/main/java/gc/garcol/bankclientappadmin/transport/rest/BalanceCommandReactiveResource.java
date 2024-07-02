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
import reactor.core.publisher.Mono;

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
    public Mono<BaseResponse> create() {
        var command = new CreateBalanceCommand();
        log.info("Create balance command: {}", command);
        return balanceReactiveDispatcher.dispatch(command);
    }

    @PostMapping("/deposit")
    public Mono<BaseResponse> deposit(@RequestBody DepositCommand command) {
        log.info("Deposit command: {}", command);
        return balanceReactiveDispatcher.dispatch(command);
    }

    @PostMapping("/withdraw")
    public Mono<BaseResponse> withdraw(@RequestBody WithdrawCommand command) {
        log.info("Withdraw command: {}", command);
        return balanceReactiveDispatcher.dispatch(command);
    }

    @PostMapping("/transfer")
    public Mono<BaseResponse> transfer(@RequestBody TransferCommand command) {
        log.info("Transfer command: {}", command);
        return balanceReactiveDispatcher.dispatch(command);
    }
}
