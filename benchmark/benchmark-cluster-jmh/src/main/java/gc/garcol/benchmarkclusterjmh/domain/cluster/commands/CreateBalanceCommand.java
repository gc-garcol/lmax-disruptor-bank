package gc.garcol.benchmarkclusterjmh.domain.cluster.commands;

import lombok.Data;

import java.util.UUID;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class CreateBalanceCommand implements BalanceCommand {
    private final String correlationId = UUID.randomUUID().toString();
}
