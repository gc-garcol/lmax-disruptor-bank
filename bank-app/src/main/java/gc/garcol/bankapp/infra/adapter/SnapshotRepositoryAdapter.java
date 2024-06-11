package gc.garcol.bankapp.infra.adapter;

import gc.garcol.bankcore.SnapshotRepository;

public class SnapshotRepositoryAdapter implements SnapshotRepository {
    @Override
    public Long getLastOffset() {
        return null;
    }

    @Override
    public void persistLastOffset(long offset) {

    }
}
