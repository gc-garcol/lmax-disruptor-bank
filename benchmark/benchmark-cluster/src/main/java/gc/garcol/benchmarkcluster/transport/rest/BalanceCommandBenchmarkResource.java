package gc.garcol.benchmarkcluster.transport.rest;

import gc.garcol.bank.proto.BalanceCommandServiceGrpc;
import gc.garcol.benchmarkcluster.infra.cluster.BalanceCommandStub;
import gc.garcol.benchmarkcluster.infra.cluster.commands.DepositCommand;
import lombok.SneakyThrows;
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
@RequestMapping("/api/balance-benchmark")
public class BalanceCommandBenchmarkResource {
    private final int MAX_CONNECTIONS = 100;
    private BalanceCommandStub[] balanceCommandStubs;
    private int connections = 1;

    public BalanceCommandBenchmarkResource(BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub) {
        balanceCommandStubs = new BalanceCommandStub[MAX_CONNECTIONS];
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            balanceCommandStubs[i] = new BalanceCommandStub(balanceCommandServiceStub);
        }
    }

    @PostMapping("/benchmark/{loop}/{connections}")
    public String benchmark(@PathVariable Integer loop, @PathVariable Integer connections) throws InterruptedException, ExecutionException {
        if (connections > MAX_CONNECTIONS) {
            return "connection must be <= " + MAX_CONNECTIONS;
        }

        this.connections = connections;

        run(10_000);
        run(loop / 2);
        run(loop);
        run(loop);
        run(loop);
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

    @SneakyThrows
    private void run(int loop) {
        Thread[] threads = new Thread[connections];
        for (int connection = 0; connection < connections; connection++) {
            int connectionIdx = connection;
            threads[connection] = new Thread(() -> {
                int reqs = connectionIdx == connections - 1 ? loop - (loop / connections) * (connections - 1) : loop / connections;
                CompletableFuture[] futures = new CompletableFuture[reqs];
                for (int i = 0; i < reqs; i++) {
                    var command = new DepositCommand();
                    command.setId(1L);
                    command.setAmount(1L);
                    futures[i] = balanceCommandStubs[connectionIdx].sendCommand(command);
                }
                CompletableFuture.allOf(futures).join();
            });
        }
        for (int connection = 0; connection < connections; connection++) {
            threads[connection].start();
        }
        for (int connection = 0; connection < connections; connection++) {
            threads[connection].join();
        }
    }
}
