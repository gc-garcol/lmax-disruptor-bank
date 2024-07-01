package gc.garcol.benchmarkclusterjmh;

import gc.garcol.benchmarkclusterjmh.domain.cluster.BalanceCommandStub;
import gc.garcol.benchmarkclusterjmh.domain.cluster.LeaderClusterCommandConfig;
import gc.garcol.benchmarkclusterjmh.domain.cluster.commands.DepositCommand;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Fork(value = 1)
@Threads(1)
@Warmup(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 1, time = 10, batchSize = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(value = Mode.All)
@Timeout(time = 10, timeUnit = TimeUnit.MINUTES)
public class BenchmarkClusterJmh {

    private BalanceCommandStub balanceCommandStub;

    @Param({"9500"})
    int leaderPort;

    @Param({"localhost"})
    String leaderHost;

    @Param({"10"})
    int requestsPerIter;

    @Setup(Level.Trial)
    public void initClient() {
        var config = new LeaderClusterCommandConfig(leaderHost, leaderPort);
        this.balanceCommandStub = new BalanceCommandStub(config.balanceCommandServiceStub());
    }

    @TearDown(Level.Trial)
    public void closeClient() throws InterruptedException {
    }

    @Benchmark
    public void deposit(Blackhole blackHole) {
        var command = new DepositCommand();
        command.setId(3L);
        command.setAmount(1L);

        CompletableFuture[] futures = new CompletableFuture[requestsPerIter];
        for (int i = 0; i < requestsPerIter; i++) {
             futures[i] = CompletableFuture.supplyAsync(() -> {
                var response = balanceCommandStub.sendCommand(command);
                 return response;
            });
        }
        CompletableFuture.allOf(futures).join();
        for (int i = 0; i < requestsPerIter; i++) {
            blackHole.consume(1);
        }
    }
}
