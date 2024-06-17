package gc.garcol.bankcluster.transport.grpc;

import gc.garcol.bank.proto.BalanceServiceGrpc;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * @author thaivc
 * @since 2024
 */
@Component
@Profile("leader")
public class BalanceCommandGrpcResource extends BalanceServiceGrpc.BalanceServiceImplBase {
}
