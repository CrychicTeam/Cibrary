package org.pickaid.pibrary.api.core;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * Typed identifier for a service registered in the {@link PibraryServiceRegistry}.
 *
 * @param id unique logical id of the service
 * @param type runtime type used for validation and lookup
 * @param <T> service contract type
 */
public record PibraryServiceKey<T>(ResourceLocation id, Class<T> type) {
    public PibraryServiceKey {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
    }
}
