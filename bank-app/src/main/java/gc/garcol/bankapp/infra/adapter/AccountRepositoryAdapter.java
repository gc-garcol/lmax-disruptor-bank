package gc.garcol.bankapp.infra.adapter;

import gc.garcol.bankcore.account.AccountRepository;
import gc.garcol.bankcore.account.Balance;

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
