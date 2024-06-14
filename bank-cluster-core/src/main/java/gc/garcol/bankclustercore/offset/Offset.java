package gc.garcol.bankclustercore.offset;

import lombok.Setter;

public class Offset {

    @Setter
    private long offset = -1;

    public long currentLastOffset() {
        return offset;
    }
    public long nextOffset() {
        return offset + 1;
    }
}
