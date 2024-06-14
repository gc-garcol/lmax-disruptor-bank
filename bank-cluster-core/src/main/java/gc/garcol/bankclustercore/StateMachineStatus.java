package gc.garcol.bankclustercore;

public enum StateMachineStatus {
    INITIALIZING,
    LOADING_SNAPSHOT,
    LOADED_SNAPSHOT,
    REPLAYING_LOGS,
    REPLAYED_LOGS,
    ACTIVE
}
