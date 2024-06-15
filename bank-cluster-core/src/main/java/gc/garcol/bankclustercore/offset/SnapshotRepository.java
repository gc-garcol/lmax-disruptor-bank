package gc.garcol.bankclustercore.offset;

/**
 * @author thaivc
 * @since 2024
 */
public interface SnapshotRepository {
    Long getLastOffset();
    void persistLastOffset(long offset);
}
