package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankcluster.infra.adapter.entities.SnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author thaivc
 * @since 2024
 */
public interface SnapshotRepositoryJpa extends JpaRepository<SnapshotEntity, String> {
}
