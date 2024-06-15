package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankcluster.infra.adapter.entities.SnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @author thaivc
 * @since 2024
 */
public interface SnapshotRepositoryJpa extends JpaRepository<SnapshotEntity, String> {

    @Modifying
    @Query("UPDATE SnapshotEntity s SET s.value = ?2 WHERE s.id = ?1")
    void update(String id, String value);

}
