package org.pickaid.pibrary.api.config;

import com.mojang.serialization.Codec;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * Typed config entry definition.
 *
 * @param id logical config id
 * @param codec codec used to load and save the value
 * @param scope config storage scope
 * @param defaultValue default value used when no data exists
 * @param <T> config value type
 */
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
