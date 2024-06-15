package gc.garcol.bankclustercore.cluster;

import lombok.Data;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class LeaderProperties {
    private int commandBufferSize = 1 << 15;
    private int replyBufferSize = 1 << 16;
}
