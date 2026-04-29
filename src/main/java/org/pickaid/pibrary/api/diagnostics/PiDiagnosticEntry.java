package org.pickaid.pibrary.api.diagnostics;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * Immutable diagnostic message emitted by Pibrary systems.
 *
 * @param level diagnostic severity
 * @param source logical source id
 * @param code stable diagnostic code
 * @param message human-readable message
 */
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
