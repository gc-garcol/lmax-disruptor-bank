package gc.garcol.bankclustercore;

import lombok.Data;

import java.util.concurrent.CompletableFuture;

@Data
public class CommandBufferEvent {
    private BaseCommand command;
    private CompletableFuture<BaseResponse> replyFuture;
}
