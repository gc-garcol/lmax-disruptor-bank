package gc.garcol.bankclustercore;

public interface SnapshotRepository {
    Long getLastOffset();
    void persistLastOffset(long offset);
}
