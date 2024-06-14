package gc.garcol.bankclustercore;

import lombok.Data;

@Data
public class ReplayBufferEvent {
    private BaseCommand command;

}
