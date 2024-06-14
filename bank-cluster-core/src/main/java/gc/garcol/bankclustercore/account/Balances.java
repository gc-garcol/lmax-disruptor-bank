package gc.garcol.bankclustercore.account;

import org.agrona.collections.Long2ObjectHashMap;

import java.util.List;

public class Balances {
    private final Long2ObjectHashMap<Balance> balances = new Long2ObjectHashMap<>();

    public List<Balance> getBalances() {
        return balances.values().stream().toList();
    }

    public void addBalance(Balance balance) {
        balances.put(balance.getId(), balance);
    }

    public Balance getBalance(long userId) {
        return balances.get(userId);
    }

    public void disableBalance(long userId) {
        balances.get(userId).inactive();
    }

    public void enableBalance(long userId) {
        balances.get(userId).active();
    }

    public void deposit(long userId, long amount) {
        balances.get(userId).increase(amount);
    }

    public void withdraw(long userId, long amount) {
        balances.get(userId).decrease(amount);
    }

}
