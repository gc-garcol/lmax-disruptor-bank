package gc.garcol.bankcore;

import gc.garcol.bankcore.account.AccountRepository;
import gc.garcol.bankcore.account.Balances;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StateMachineManagerImpl implements StateMachineManager {

    private final AccountRepository accountRepository;
    private final SnapshotRepository snapshotRepository;
    private final Balances balances;
    @Override
    public void reloadSnapshot() {
    }

    @Override
    public void takeSnapshot() {

    }

    @Override
    public void replayLogs() {
    }
}
