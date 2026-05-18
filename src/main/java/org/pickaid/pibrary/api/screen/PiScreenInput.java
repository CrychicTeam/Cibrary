package org.pickaid.pibrary.api.screen;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record PiScreenInput(
        ResourceLocation scope,
        PiScreenPoint point,
        PiScreenInputKind kind,
        int code,
        int modifiers,
        long frame
) {
    public PiScreenInput {
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(point, "point");
        Objects.requireNonNull(kind, "kind");
    }
}
