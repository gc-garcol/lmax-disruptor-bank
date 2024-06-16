package gc.garcol.bankcluster.infra.adapter.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "snapshots")
public class SnapshotEntity {

    @Id
    private String id;

    private String value;

}
