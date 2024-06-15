package gc.garcol.bankclustercore;

/**
 * @author thaivc
 * @since 2024
 */
public interface BufferEventDispatcher<T extends BufferEvent> {
    void dispatch(T event);
}
