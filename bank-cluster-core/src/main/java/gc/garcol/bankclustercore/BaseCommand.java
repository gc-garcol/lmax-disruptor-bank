package gc.garcol.bankclustercore;

import gc.garcol.bank.proto.BalanceProto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author thaivc
 * @since 2024
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BaseCommand {
    private BalanceProto.CommandLog commandLog;
}
