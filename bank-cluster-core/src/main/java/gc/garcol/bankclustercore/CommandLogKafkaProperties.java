package gc.garcol.bankclustercore;

import lombok.Data;

@Data
public class CommandLogKafkaProperties {
    private String topic;
    private String groupId;
}
