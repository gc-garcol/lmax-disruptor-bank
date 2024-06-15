package gc.garcol.bankcluster.infra.adapter;

import gc.garcol.bankcluster.infra.adapter.entities.BalanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author thaivc
 * @since 2024
 */
public interface AccountRepositoryJpa extends JpaRepository<BalanceEntity, Long> {
}
