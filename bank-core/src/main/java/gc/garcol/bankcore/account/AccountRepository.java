package gc.garcol.bankcore.account;

import java.util.List;
import java.util.stream.Stream;

public interface AccountRepository {
    Stream<Balance> balances();
    void persist(List<Balance> balances);
}
