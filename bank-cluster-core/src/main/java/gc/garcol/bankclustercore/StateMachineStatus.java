package gc.garcol.bankclustercore;

/**
 * @author thaivc
 * @since 2024
 */
public enum StateMachineStatus {
    INITIALIZING,
    LOADING_SNAPSHOT,
    LOADED_SNAPSHOT,
    REPLAYING_LOGS,
    REPLAYED_LOGS,
    ACTIVE
}
