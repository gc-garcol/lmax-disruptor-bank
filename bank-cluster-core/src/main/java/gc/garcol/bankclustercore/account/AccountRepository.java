package gc.garcol.bankclustercore.account;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author thaivc
 * @since 2024
 */
public interface AccountRepository {
    Stream<Balance> balances();
    Long lastedId();
    void persistBalances(List<Balance> balances);
    void persistLastId(Long id);
}
