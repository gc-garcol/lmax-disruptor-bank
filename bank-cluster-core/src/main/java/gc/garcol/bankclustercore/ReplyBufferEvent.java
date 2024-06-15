package gc.garcol.bankclustercore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author thaivc
 * @since 2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyBufferEvent implements BufferEvent {
    private String replyChannel;
    private String correlationId;
    private BaseResult result;
}
