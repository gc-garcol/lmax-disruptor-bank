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
    private final Long2ObjectHashMap<Balance> changedBalances = new Long2ObjectHashMap<>();

    @Getter
    @Setter
    private long lastedId = 0;

    @Getter
    @Setter
    private boolean enableChangedCapture = false;

    public List<Balance> getChangedBalances() {
        return changedBalances.values().stream().toList();
    }

    public void clearChangedBalances() {
        changedBalances.clear();
    }

    public long newBalance() {
        lastedId++;
        balances.put(lastedId, new Balance(lastedId, 0, 2, true));

        if (enableChangedCapture) {
            changedBalances.put(lastedId, new Balance(lastedId, 0, 2, true));
        }
        return lastedId;
    }

    public void deposit(long id, long amount) {
        balances.get(id).increase(amount);
        captureBalance(id);
    }

    public void withdraw(long id, long amount) {
        balances.get(id).decrease(amount);
        captureBalance(id);
    }

    public void transfer(long fromId, long toId, long amount) {
        withdraw(fromId, amount);
        deposit(toId, amount);
        captureBalance(fromId);
        captureBalance(toId);
    }

    public void putBalance(Balance balance) {
        balances.put(balance.getId(), balance);
    }

    public Balance getBalance(long id) {
        return balances.get(id);
    }

    private void captureBalance(long id) {
        if (enableChangedCapture) {
            var balance = balances.get(id);
            changedBalances.put(
                lastedId,
                new Balance(balance.getId(), balance.getAmount(), balance.getPrecision(), balance.isActive())
            );
        }
    }
}
