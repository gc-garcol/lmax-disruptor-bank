package gc.garcol.bankcore;

import lombok.Data;

import java.util.concurrent.CompletableFuture;

@Data
public class CommandBufferEvent {
    private BaseCommand command;
    private CompletableFuture<BaseResponse> replyFuture;
}
