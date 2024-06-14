package gc.garcol.common.exception;

/**
 * @author thaivc
 * @since 2024
 */
public class BankException extends RuntimeException {

    public BankException(String message) {
        super(message);
    }

    public BankException(String message, Throwable cause) {
        super(message, cause);
    }
}
