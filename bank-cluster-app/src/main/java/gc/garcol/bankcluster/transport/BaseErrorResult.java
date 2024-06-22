package gc.garcol.bankcluster.transport;

import gc.garcol.bankclustercore.BaseResult;
import lombok.*;

/**
 * @author thaivc
 * @since 2024
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class BaseErrorResult extends BaseResult {
    private String message;
    private int code;

    @Override
    public String toString() {
        return String.format("%s::%s", code, message);
    }
}
