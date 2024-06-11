package gc.garcol.common.exception;

/**
 * @author thaivc
 * @since 2024
 */
public class Bank4xxException extends BankException {

    public Bank4xxException(String message) {
        super(message);
    }

    public Bank4xxException(String message, Throwable cause) {
        super(message, cause);
    }
}
