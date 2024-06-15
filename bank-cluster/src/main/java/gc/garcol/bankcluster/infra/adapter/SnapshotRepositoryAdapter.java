package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankclustercore.offset.SnapshotRepository;
import org.springframework.stereotype.Component;

/**
 * @author thaivc
 * @since 2024
 */
@Component
public class SnapshotRepositoryAdapter implements SnapshotRepository {
    @Override
    public Long getLastOffset() {
        return null;
    }

    @Override
    public void persistLastOffset(long offset) {

    }
}
