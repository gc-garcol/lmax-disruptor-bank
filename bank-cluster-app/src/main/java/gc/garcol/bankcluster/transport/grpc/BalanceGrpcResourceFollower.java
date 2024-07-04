package gc.garcol.bankcluster.transport.grpc;

import com.google.common.util.concurrent.MoreExecutors;
import gc.garcol.bankclustercore.account.Balances;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@Profile("follower")
@Component
@RequiredArgsConstructor
public class BalanceGrpcResourceFollower {
    private final Balances balances;
    private Server server;

    @Value("${server.grpc.port}")
    private int serverPort;

    @PostConstruct
    void init() {
        try {
            BalanceGrpcQuery balanceGrpcQuery = new BalanceGrpcQuery(balances);
            server = ServerBuilder.forPort(serverPort)
                .addService(balanceGrpcQuery)
                .executor(MoreExecutors.directExecutor())
                .build();
            server.start();
        } catch (Exception e) {
            log.error("Failed to start gRPC server", e);
            System.exit(-9);
        }
    }

    @PreDestroy
    void destroy() {
        if (server != null) {
            server.shutdown();
        }
    }
}
