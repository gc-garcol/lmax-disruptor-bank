package gc.garcol.bankclustercore.account;

import gc.garcol.bankclustercore.BaseResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author thaivc
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode(callSuper = false)
public class BalanceResult extends BaseResult {
    private String message;
    private int code;
}
