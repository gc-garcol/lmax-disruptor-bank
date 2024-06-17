package gc.garcol.bankcluster.infra;

import gc.garcol.bank.proto.BalanceProto;
import io.grpc.stub.StreamObserver;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author thaivc
 * @since 2024
 */
@Profile("leader")
@Component
public class SimpleReplier {
    public final Map<String, StreamObserver<BalanceProto.BaseResult>> repliers = new ConcurrentHashMap<>();
}
