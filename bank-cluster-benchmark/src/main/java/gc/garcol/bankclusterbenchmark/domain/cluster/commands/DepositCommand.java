package gc.garcol.bankclusterbenchmark.domain.cluster.commands;

import lombok.Data;

import java.util.UUID;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class DepositCommand implements BalanceCommand {
    private final String correlationId = UUID.randomUUID().toString();
    private Long id;
    private Long amount;
}
