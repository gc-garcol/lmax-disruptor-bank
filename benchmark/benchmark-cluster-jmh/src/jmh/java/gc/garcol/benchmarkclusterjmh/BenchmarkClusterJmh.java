package gc.garcol.benchmarkclusterjmh;

import gc.garcol.benchmarkclusterjmh.domain.cluster.BalanceCommandStub;
import gc.garcol.benchmarkclusterjmh.domain.cluster.LeaderClusterCommandConfig;
import gc.garcol.benchmarkclusterjmh.domain.cluster.commands.DepositCommand;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class BenchmarkClusterJmh {

    private static final int leaderPort = 9500;
    private static final String leaderHost = "localhost";

    private BalanceCommandStub balanceCommandStub;

    @Setup(Level.Trial)
    public void initClient() {
        var config = new LeaderClusterCommandConfig(leaderHost, leaderPort);
        balanceCommandStub = new BalanceCommandStub(config.balanceCommandServiceStub());
    }

    @TearDown(Level.Trial)
    public void closeClient() {
        balanceCommandStub.closeConnection();
    }

    @Threads(8)
    @Fork(value = 1, jvmArgs = {"-XX:MaxDirectMemorySize=512M"})
    @Warmup(iterations = 1, time = 5)
    @Measurement(iterations = 1, time = 10)
    @BenchmarkMode(value = {Mode.Throughput, Mode.AverageTime})
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Timeout(time = 1, timeUnit = TimeUnit.MINUTES)
    @Benchmark
    public void deposit(Blackhole blackHole) throws InterruptedException {
        var command = new DepositCommand();
        command.setId(1L);
        command.setAmount(1L);
        balanceCommandStub
            .sendCommand(command, () -> blackHole.consume(1));
    }
}
