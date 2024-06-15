package gc.garcol.bankclustercore;

import lombok.Data;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class ReplayBufferEvent implements BufferEvent {
    private BaseCommand command;
}
