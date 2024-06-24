package gc.garcol.bankclustercore.cluster;

import lombok.Data;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class FollowerProperties {
    private int bufferSize = 1 << 10;
    private int pollingInterval = 100;
}
