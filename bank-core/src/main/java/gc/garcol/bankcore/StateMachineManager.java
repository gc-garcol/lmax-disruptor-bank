package gc.garcol.bankcore;

public interface StateMachineManager {
    void reloadSnapshot();

    void takeSnapshot();

    void replayLogs();
}
