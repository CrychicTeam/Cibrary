package org.pickaid.pibrary.api.signal;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record PiSignalType<T>(ResourceLocation id, Class<T> payloadType) {
    public PiSignalType {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(payloadType, "payloadType");
    }

    public static <T> PiSignalType<T> create(ResourceLocation id, Class<T> payloadType) {
        return new PiSignalType<>(id, payloadType);
    }
}
