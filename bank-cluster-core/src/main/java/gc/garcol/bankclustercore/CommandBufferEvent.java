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
public class CommandBufferEvent implements BufferEvent {
    private String replyChannel;
    private String correlationId;

    private BaseCommand command;
    private BaseResult result;

    public CommandBufferEvent(String replyChannel, String correlationId, BaseCommand command) {
        this.replyChannel = replyChannel;
        this.correlationId = correlationId;
        this.command = command;
    }

    public void copy(CommandBufferEvent event) {
        this.replyChannel = event.replyChannel;
        this.correlationId = event.correlationId;
        this.command = event.command;
        this.result = event.result;
    }
}
