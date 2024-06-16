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

    public void copy(CommandBufferEvent event) {
        this.replyChannel = event.replyChannel;
        this.correlationId = event.correlationId;
        this.command = event.command;
        this.result = event.result;
    }
}
