package gc.garcol.benchmarkcluster.transport.rest;

import gc.garcol.bank.proto.BalanceCommandServiceGrpc;
import gc.garcol.benchmarkcluster.infra.cluster.BalanceCommandStub;
import gc.garcol.benchmarkcluster.common.commands.DepositCommand;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
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
    private final int MAX_CONNECTIONS = 200;
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

        run(10_000, true);
        run(loop / 2, true);
        run(loop, true);
        run(loop, true);
        run(loop, true);
        var start = System.currentTimeMillis();
        run(loop, false);
        var end = System.currentTimeMillis();
        var executionTime = end - start;
        var throughput = (double) loop / (executionTime * 1.0 / 1000);

        List<Long> latencies = new ArrayList<>();
        for (int connection = 0; connection < connections; connection++) {
            var subLatencies = balanceCommandStubs[connection].replyFutures.values()
                .stream().map(reply -> reply.getEndTime() - reply.getStartTime())
                .toList();
            latencies.addAll(subLatencies);
            balanceCommandStubs[connection].replyFutures.clear();
        }

        latencies.sort(Long::compare);

        var result =
            String.format("Start time: %d ms%n%n", start) +
            String.format("End time: %d ms%n%n", end) +
            String.format("Execution time: %d ms%n%n", executionTime) +
            String.format("Throughput: %.2f ops/s%n%n", throughput) +
            "Latencies: \n" +
            String.format("p10 = %.2f ms %n", latencies.get((int) Math.round(latencies.size() * 0.10)) * 1.0 / 1_000_000) +
            String.format("p25 = %.2f ms %n", latencies.get((int) Math.round(latencies.size() * 0.25)) * 1.0 / 1_000_000) +
            String.format("p50 = %.2f ms %n", latencies.get((int) Math.round(latencies.size() * 0.5)) * 1.0 / 1_000_000) +
            String.format("p75 = %.2f ms %n", latencies.get((int) Math.round(latencies.size() * 0.75)) * 1.0 / 1_000_000) +
            String.format("p99 = %.2f ms %n%n", latencies.get((int) Math.round(latencies.size() * 0.99)) * 1.0 / 1_000_000) +
            "Result: Completed " + loop + " operations in " + executionTime + " ms";
        System.out.println(result);
        return result;
    }

    @SneakyThrows
    private void run(int loop, boolean clean) {
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
            if (clean) {
                balanceCommandStubs[connection].replyFutures.clear();
            }
        }
    }
}
