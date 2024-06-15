package gc.garcol.bankcluster.infra.adapter.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author thaivc
 * @since 2024
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
public class BalanceEntity {

    @Id
    private Long id;

    private long amount;

    private int precision;

    private boolean active;

}
