package gc.garcol.bankcluster.infra.adapter.entities;

import lombok.Getter;

/**
 * @author thaivc
 * @since 2024
 */
@Getter
public enum SnapshotType {

    LAST_KAFKA_OFFSET("LAST_KAFKA_OFFSET"),
    LAST_BALANCE_ID("LAST_BALANCE_ID");

    private final String type;

    SnapshotType(String type) {
        this.type = type;
    }

}
