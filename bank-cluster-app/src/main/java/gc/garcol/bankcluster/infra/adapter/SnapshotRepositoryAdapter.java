package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankcluster.infra.adapter.entities.SnapshotEntity;
import gc.garcol.bankcluster.infra.adapter.entities.SnapshotType;
import gc.garcol.bankclustercore.offset.SnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author thaivc
 * @since 2024
 */
@Component
@RequiredArgsConstructor
public class SnapshotRepositoryAdapter implements SnapshotRepository {

    private final SnapshotRepositoryJpa snapshotRepositoryJpa;

    @Override
    public Long getLastOffset() {
        return snapshotRepositoryJpa.findById(SnapshotType.LAST_KAFKA_OFFSET.getType())
            .map(SnapshotEntity::getValue)
            .map(Long::parseLong)
            .orElse(-1L);
    }

    @Override
    public void persistLastOffset(long offset) {
        snapshotRepositoryJpa.update(SnapshotType.LAST_KAFKA_OFFSET.getType(), String.valueOf(offset));
    }
}
