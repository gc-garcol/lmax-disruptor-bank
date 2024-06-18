package gc.garcol.bankclustercore;

/**
 * @author thaivc
 * @since 2024
 */
public interface TransactionManager<T> {
    void doInNewTransaction(Runnable runnable);
}
