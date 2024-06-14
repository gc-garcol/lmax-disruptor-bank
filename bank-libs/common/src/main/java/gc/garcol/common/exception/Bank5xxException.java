package gc.garcol.common.exception;

/**
 * @author thaivc
 * @since 2024
 */
public class Bank5xxException extends BankException {
    public Bank5xxException(String message) {
        super(message);
    }

    public Bank5xxException(String message, Throwable cause) {
        super(message, cause);
    }
}
