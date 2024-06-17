package gc.garcol.bankcluster.transport.grpc;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author thaivc
 * @since 2024
 */
@Component
@Profile({"leader", "follower"})
public class BalanceQueryGrpcResource {
}
