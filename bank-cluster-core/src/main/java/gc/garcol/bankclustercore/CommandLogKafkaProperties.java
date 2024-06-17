package gc.garcol.bankclustercore;

import lombok.Data;

/**
 * @author thaivc
 * @since 2024
 */
@Data
public class CommandLogKafkaProperties {
    private String topic;
    private String groupId;
    private long nextOffset;
}
