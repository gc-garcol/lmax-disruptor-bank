package gc.garcol.benchmarkclusterjmh.domain.cluster;

import gc.garcol.bank.proto.BalanceCommandServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author thaivc
 * @since 2024
 */
@Setter
@NoArgsConstructor
public class LeaderClusterCommandConfig {

    private String leaderHost = "127.0.0.1";

    private int leaderPort = 9500;

    public LeaderClusterCommandConfig(String leaderHost, int leaderPort) {
        this.leaderHost = leaderHost;
        this.leaderPort = leaderPort;
    }

    public BalanceCommandServiceGrpc.BalanceCommandServiceStub balanceCommandServiceStub() {
        return BalanceCommandServiceGrpc.newStub(
            ManagedChannelBuilder.forAddress(leaderHost, leaderPort)
                .usePlaintext()
                .build()
        );
    }
}
