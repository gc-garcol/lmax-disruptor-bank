package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankclustercore.offset.SnapshotRepository;

public class SnapshotRepositoryAdapter implements SnapshotRepository {
    @Override
    public Long getLastOffset() {
        return null;
    }

    @Override
    public void persistLastOffset(long offset) {

    }
}
