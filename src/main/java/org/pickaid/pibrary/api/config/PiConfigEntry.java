package org.pickaid.pibrary.api.config;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record PiConfigEntry<T>(
        ResourceLocation id,
        Codec<T> codec,
        PiConfigScope scope,
        T defaultValue
) {
    public PiConfigEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(scope, "scope");
    }
}
