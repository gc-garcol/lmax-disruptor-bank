package gc.garcol.bankcluster.transport.rest;

import gc.garcol.bankcluster.transport.BaseErrorResponse;
import gc.garcol.common.exception.Bank4xxException;
import gc.garcol.common.exception.Bank5xxException;
import gc.garcol.common.exception.BankException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(Bank4xxException.class)
    public ResponseEntity<BaseErrorResponse> handle4xxException(Bank4xxException exception) {
        log.error("4xx Exception", exception);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST.value())
                .body(new BaseErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(Bank5xxException.class)
    public ResponseEntity<BaseErrorResponse> handle5xxException(Bank5xxException exception) {
        log.error("5xx Exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new BaseErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

    @ExceptionHandler(BankException.class)
    public ResponseEntity<BaseErrorResponse> handleBankException(Bank5xxException exception) {
        log.error("Unknown exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new BaseErrorResponse(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }

}
