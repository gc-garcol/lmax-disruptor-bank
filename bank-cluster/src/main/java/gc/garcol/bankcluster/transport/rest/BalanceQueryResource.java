package gc.garcol.bankcluster.transport.rest;

import gc.garcol.bankclustercore.account.Balance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/v1/balance-query-sync")
public class BalanceQueryResource {

    @GetMapping
    public ResponseEntity<Balance> queryBalance() {
        return null;
    }

}
