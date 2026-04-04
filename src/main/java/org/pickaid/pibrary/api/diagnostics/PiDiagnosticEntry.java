package org.pickaid.pibrary.api.diagnostics;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record PiDiagnosticEntry(
        PiDiagnosticLevel level,
        ResourceLocation source,
        String code,
        String message
) {
    public PiDiagnosticEntry {
        Objects.requireNonNull(level, "level");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
    }
}
