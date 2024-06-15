package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankclustercore.account.AccountRepository;
import gc.garcol.bankclustercore.account.Balance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author thaivc
 * @since 2024
 */
@Component
public class AccountRepositoryAdapter implements AccountRepository {
    @Override
    public Stream<Balance> balances() {
        return null;
    }

    @Override
    public Long lastedId() {
        return 0L;
    }

    @Override
    public void persistBalances(List<Balance> balances) {
    }

    @Override
    public void persistLastId(Long id) {
    }
}
