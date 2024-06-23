package gc.garcol.bankclustercore.account;

import gc.garcol.common.exception.Bank4xxException;
import lombok.Getter;
import lombok.Setter;
import org.agrona.collections.Long2ObjectHashMap;

import java.util.List;
import java.util.Optional;

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
        captureBalance(lastedId);
        return lastedId;
    }

    public void deposit(long id, long amount) {
        if (amount <= 0) {
            throw new Bank4xxException("Amount must be greater than 0");
        }
        Optional.ofNullable(balances.get(id))
            .orElseThrow(() -> new Bank4xxException(String.format("Balance %s not found", id)))
            .increase(amount);
        captureBalance(id);
    }

    public void withdraw(long id, long amount) {
        if (amount <= 0) {
            throw new Bank4xxException("Amount must be greater than 0");
        }
        Optional.ofNullable(balances.get(id))
            .orElseThrow(() -> new Bank4xxException(String.format("Balance %s not found", id)))
            .decrease(amount);
        captureBalance(id);
    }

    public void transfer(long fromId, long toId, long amount) {
        if (fromId == toId) {
            throw new Bank4xxException("FromId and ToId must be different");
        }
        if (amount <= 0) {
            throw new Bank4xxException("Amount must be greater than 0");
        }

        if (!balances.containsKey(fromId)) {
            throw new Bank4xxException(String.format("Balance %s not found", fromId));
        }
        if (!balances.containsKey(toId)) {
            throw new Bank4xxException(String.format("Balance %s not found", toId));
        }

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
            changedBalances.put(id, new Balance(balance.getId(), balance.getAmount(), balance.getPrecision(), balance.isActive()));
        }
    }
}
