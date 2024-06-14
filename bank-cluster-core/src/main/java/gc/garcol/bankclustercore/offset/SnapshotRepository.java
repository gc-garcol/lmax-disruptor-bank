package gc.garcol.bankclustercore.offset;

public interface SnapshotRepository {
    Long getLastOffset();
    void persistLastOffset(long offset);
}
