package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankclustercore.account.AccountRepository;
import gc.garcol.bankclustercore.account.Balance;

import java.util.List;
import java.util.stream.Stream;

public class AccountRepositoryAdapter implements AccountRepository {
    @Override
    public Stream<Balance> balances() {
        return null;
    }

    @Override
    public void persist(List<Balance> balances) {

    }
}
