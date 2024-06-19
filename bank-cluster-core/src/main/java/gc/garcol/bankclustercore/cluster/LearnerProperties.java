package gc.garcol.bankclustercore.cluster;

import lombok.Data;

import java.time.Duration;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class LearnerProperties {
    private int bufferSize = 1 << 5;
    private int pollingInterval = 100;
    private int maxSnapshotCheckCircles = 50;
    private int snapshotFragmentSize = 10_000;
    private Duration snapshotLifeTime = Duration.ofSeconds(5);
}
