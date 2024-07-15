package gc.garcol.bankclientappadmin.common.query;

import lombok.Data;

import java.util.UUID;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class BalanceDetailQuery implements BalanceQuery {
    private final String correlationId = UUID.randomUUID().toString();
    private Long id;
}
