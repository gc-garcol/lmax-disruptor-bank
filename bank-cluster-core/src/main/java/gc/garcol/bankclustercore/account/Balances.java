package gc.garcol.bankclustercore.account;

import lombok.Getter;
import lombok.Setter;
import org.agrona.collections.Long2ObjectHashMap;

import java.util.List;

/**
 * @author thaivc
 * @since 2024
 */
public class Balances {
    private final Long2ObjectHashMap<Balance> balances = new Long2ObjectHashMap<>();

    @Getter
    @Setter
    private long lastedId = 0;

    public List<Balance> getBalances() {
        return balances.values().stream().toList();
    }

    public long newBalance() {
        lastedId++;
        balances.put(lastedId, Balance.builder()
                .id(lastedId)
                .active(true)
                .amount(0)
                .precision(2)
            .build());
        return lastedId;
    }

    public void deposit(long id, long amount) {
        balances.get(id).increase(amount);
    }

    public void withdraw(long id, long amount) {
        balances.get(id).decrease(amount);
    }

    public void transfer(long fromId, long toId, long amount) {
        withdraw(fromId, amount);
        deposit(toId, amount);
    }
    
    public void putBalance(Balance balance) {
        balances.put(balance.getId(), balance);
    }

    public Balance getBalance(long id) {
        return balances.get(id);
    }

    public void disableBalance(long id) {
        balances.get(id).inactive();
    }

    public void enableBalance(long id) {
        balances.get(id).active();
    }
}
