package org.pickaid.pibrary.api.registrate;

import java.util.Objects;

public record PiRegistrateContext(String modId) {
    public PiRegistrateContext {
        Objects.requireNonNull(modId, "modId");
        if (modId.isBlank()) {
            throw new IllegalArgumentException("modId must not be blank");
        }
    }
}
