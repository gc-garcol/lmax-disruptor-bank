package gc.garcol.bankcore;

import lombok.Data;

@Data
public class ReplayBufferEvent {
    private BaseCommand command;

}
