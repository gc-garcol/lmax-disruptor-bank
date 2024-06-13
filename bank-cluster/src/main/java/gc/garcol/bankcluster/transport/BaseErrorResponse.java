package gc.garcol.bankcluster.transport;

import gc.garcol.bankclustercore.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class BaseErrorResponse extends BaseResponse {
    private String message;
    private int code;
}
