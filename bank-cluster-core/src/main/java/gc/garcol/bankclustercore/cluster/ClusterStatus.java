package gc.garcol.bankclustercore.cluster;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author thaivc
 * @since 2024
 */
public enum ClusterStatus {
    NOT_AVAILABLE,
    ACTIVE;

    public static final AtomicReference<ClusterStatus> STATE = new AtomicReference<>(NOT_AVAILABLE);
}
