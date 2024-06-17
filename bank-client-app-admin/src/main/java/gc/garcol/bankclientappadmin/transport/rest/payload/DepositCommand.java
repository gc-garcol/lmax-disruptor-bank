package gc.garcol.bankclientappadmin.transport.rest.payload;

import lombok.Data;

import java.util.UUID;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class DepositCommand {
    private final String correlationId = UUID.randomUUID().toString();
    private Long amount;
}
