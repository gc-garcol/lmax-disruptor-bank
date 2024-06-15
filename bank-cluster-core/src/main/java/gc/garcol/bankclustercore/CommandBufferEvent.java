package gc.garcol.bankclustercore;

import lombok.Data;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class CommandBufferEvent implements BufferEvent {
    private String replyChannel;
    private String correlationId;

    private BaseCommand command;
    private BaseResult result;
}
