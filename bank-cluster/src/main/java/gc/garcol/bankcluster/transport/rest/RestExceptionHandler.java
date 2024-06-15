package gc.garcol.bankcluster.transport.rest;

import gc.garcol.bankcluster.transport.BaseErrorResult;
import gc.garcol.common.exception.Bank4xxException;
import gc.garcol.common.exception.Bank5xxException;
import gc.garcol.common.exception.BankException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author thaivc
 * @since 2024
 */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(Bank4xxException.class)
    public ResponseEntity<BaseErrorResult> handle4xxException(Bank4xxException exception) {
        log.error("4xx Exception", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(new BaseErrorResult(exception.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(Bank5xxException.class)
    public ResponseEntity<BaseErrorResult> handle5xxException(Bank5xxException exception) {
        log.error("5xx Exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new BaseErrorResult(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(BankException.class)
    public ResponseEntity<BaseErrorResult> handleBankException(Bank5xxException exception) {
        log.error("Unknown exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new BaseErrorResult(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

}
