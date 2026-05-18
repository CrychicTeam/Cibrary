package org.pickaid.pibrary.api.signal;

import java.util.List;
import java.util.Objects;

public record PiSignalTrace(List<PiSignalType<?>> chain) {
    public PiSignalTrace {
        chain = List.copyOf(Objects.requireNonNull(chain, "chain"));
    }

    public static PiSignalTrace empty() {
        return new PiSignalTrace(List.of());
    }
}
