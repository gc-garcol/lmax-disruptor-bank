package gc.garcol.benchmarkcluster.common;

import lombok.Data;

import java.util.concurrent.CompletableFuture;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class BaseReply {
    public BaseReply(long startTime) {
        this.startTime = startTime;
        this.future = new CompletableFuture<>();
    }
    private CompletableFuture<Long> future;
    private long startTime;
    private long endTime;
}
