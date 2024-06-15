package gc.garcol.bankclustercore.account;

import gc.garcol.bankclustercore.BaseResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author thaivc
 * @since 2024
 */
@Data
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode(callSuper = false)
public class BalanceResult extends BaseResult {
    private String message;
    private int code;
}
