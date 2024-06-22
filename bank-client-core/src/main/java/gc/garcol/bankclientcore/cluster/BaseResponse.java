package gc.garcol.bankclientcore.cluster;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author thaivc
 * @since 2024
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse {
    protected int code;
    protected String message;
}
