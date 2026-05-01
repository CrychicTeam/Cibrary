package org.pickaid.pibrary.api.config;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * One generated datapack config file.
 *
 * @param type config family
 * @param id file id exposed to runtime code
 * @param value value written to JSON
 * @param <T> config value type
 */
public record PiDataConfigEntry<T>(
        PiDataConfigType<T> type,
        ResourceLocation id,
        T value
) {
    public PiDataConfigEntry {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(value, "value");
    }

    public ResourceLocation resourceId() {
        return type.resourceId(id);
    }

    public String generatedPath(String directory) {
        return type.generatedPath(directory, id);
    }
}
