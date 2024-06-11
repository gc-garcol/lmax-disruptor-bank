package gc.garcol.bankcore;

public interface SnapshotRepository {
    Long getLastOffset();
    void persistLastOffset(long offset);
}
