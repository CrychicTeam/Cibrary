package org.pickaid.pibrary.api.core;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record PibraryServiceKey<T>(ResourceLocation id, Class<T> type) {
    public PibraryServiceKey {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
    }
}
