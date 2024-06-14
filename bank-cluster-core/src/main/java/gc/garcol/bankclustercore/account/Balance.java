package gc.garcol.bankclustercore.account;

import gc.garcol.common.exception.Bank4xxException;
import lombok.Data;

@Data
public class Balance {
    private long id;
    private long amount;
    private int precision;
    private boolean active;

    public void increase(long amount) {
        this.amount += amount;
    }

    public void decrease(long amount) {
        if (this.amount < amount) {
            throw new Bank4xxException("Insufficient balance");
        }
        this.amount -= amount;
    }

    public void active() {
        this.active = true;
    }

    public void inactive() {
        this.active = false;
    }
}
