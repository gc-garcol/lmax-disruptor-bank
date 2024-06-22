package gc.garcol.bankclientappadmin.infra.command;

import lombok.Data;

import java.util.UUID;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class TransferCommand implements BalanceCommand {
    private final String correlationId = UUID.randomUUID().toString();
    private Long fromId;
    private Long toId;
    private Long amount;
}
