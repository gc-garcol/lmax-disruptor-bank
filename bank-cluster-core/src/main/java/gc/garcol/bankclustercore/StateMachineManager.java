package gc.garcol.bankclustercore;

public interface StateMachineManager {
    void reloadSnapshot();

    void takeSnapshot();

    void replayLogs();
}
