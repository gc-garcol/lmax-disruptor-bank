package gc.garcol.bankclustercore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author thaivc
 * @since 2024
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BaseResultError extends BaseResult {

    private String message;

    public static final BaseResultError COMMAND_NOT_FOUND = new BaseResultError("Command not found");

    @Override
    public String toString() {
        return String.format("%s::%s", 500, message);
    }
}
