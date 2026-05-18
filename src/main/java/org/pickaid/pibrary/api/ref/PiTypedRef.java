package org.pickaid.pibrary.api.ref;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record PiTypedRef<T>(ResourceLocation id, Class<T> valueType) {
    public PiTypedRef {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(valueType, "valueType");
    }

    public static <T> PiTypedRef<T> of(ResourceLocation id, Class<T> valueType) {
        return new PiTypedRef<>(id, valueType);
    }
}
