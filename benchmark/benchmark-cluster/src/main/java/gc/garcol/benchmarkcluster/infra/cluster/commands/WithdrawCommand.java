package gc.garcol.benchmarkcluster.infra.cluster.commands;

import lombok.Data;

import java.util.UUID;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class WithdrawCommand implements BalanceCommand {
    private final String correlationId = UUID.randomUUID().toString();
    private Long id;
    private Long amount;
}
