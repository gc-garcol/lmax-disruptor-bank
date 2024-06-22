package gc.garcol.bankclientappadmin.infra.response;

import gc.garcol.bankclientcore.cluster.BaseResponse;
import lombok.*;

/**
 * @author thaivc
 * @since 2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class BalanceDetailResponse extends BaseResponse {
    private long id;
    private long amount;
}
