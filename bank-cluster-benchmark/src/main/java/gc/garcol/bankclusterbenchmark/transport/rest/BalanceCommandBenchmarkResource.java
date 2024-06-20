package gc.garcol.bankclusterbenchmark.transport.rest;

import gc.garcol.bankclusterbenchmark.domain.cluster.BalanceCommandStub;
import gc.garcol.bankclusterbenchmark.domain.cluster.commands.DepositCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/balance-benchmark")
public class BalanceCommandBenchmarkResource {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private final BalanceCommandStub balanceCommandStub;

    @PostMapping("/warmup/{loop}")
    public String warmup(@PathVariable Integer loop) throws ExecutionException, InterruptedException {
        run(loop);
        return "Warmup done!";
    }

    @PostMapping("/benchmark/{loop}")
    public String benchmark(@PathVariable Integer loop) throws InterruptedException, ExecutionException {
        var start = System.currentTimeMillis();
        run(loop);
        var end = System.currentTimeMillis();
        var executionTime = end - start;
        var throughput = (double) loop / (executionTime * 1.0 / 1000);

        var result =
            String.format("Start time: %d ms%n%n", start) +
            String.format("End time: %d ms%n%n", end) +
            String.format("Execution time: %d ms%n%n", executionTime) +
            String.format("Throughput: %.2f ops/s%n%n", throughput) +
            "Result: Completed " + loop + " operations in " + executionTime + " ms";
        System.out.println(result);
        return result;
    }

    private void run(int loop) {
        CompletableFuture[] futures = new CompletableFuture[loop];

        for (int i = 0; i < loop; i++) {
            var command = new DepositCommand();
            command.setId(1L);
            command.setAmount(1L);
            futures[i] = balanceCommandStub.sendCommand(command);
        }
        CompletableFuture.allOf(futures).join();
    }
}
