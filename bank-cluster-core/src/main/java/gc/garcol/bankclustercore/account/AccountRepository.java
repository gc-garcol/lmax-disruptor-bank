package gc.garcol.bankclustercore.account;

import java.util.List;
import java.util.stream.Stream;

public interface AccountRepository {
    Stream<Balance> balances();
    void persist(List<Balance> balances);
}
