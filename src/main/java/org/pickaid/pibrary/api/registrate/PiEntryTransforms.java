package org.pickaid.pibrary.api.registrate;

import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import java.util.Objects;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/**
 * Transforms that work on any Registrate builder.
 *
 * <p>Use this class for entry-level concerns: register callbacks, datagen hooks,
 * and post-registration ordering. Use {@link PiBlockTransforms} and
 * {@link PiItemTransforms} only when the operation is genuinely block- or
 * item-specific.</p>
 */
public final class PiEntryTransforms {
    private PiEntryTransforms() {
    }

    public static <R, T extends R, P, S extends Builder<R, T, P, S>> NonNullFunction<S, S>
    onRegister(NonNullConsumer<? super T> action) {
        Objects.requireNonNull(action, "action");
        return builder -> builder.onRegister(action);
    }

    public static <R, T extends R, OR, P, S extends Builder<R, T, P, S>> NonNullFunction<S, S>
    onRegisterAfter(ResourceKey<? extends Registry<OR>> registry, NonNullConsumer<? super T> action) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(action, "action");
        return builder -> builder.onRegisterAfter(registry, action);
    }

    public static <R, T extends R, P, S extends Builder<R, T, P, S>, D extends RegistrateProvider> NonNullFunction<S, S>
    data(ProviderType<? extends D> providerType, NonNullBiConsumer<DataGenContext<R, T>, D> action) {
        Objects.requireNonNull(providerType, "providerType");
        Objects.requireNonNull(action, "action");
        return builder -> builder.setData(providerType, action);
    }

    public static <R, T extends R, P, S extends Builder<R, T, P, S>, D extends RegistrateProvider> NonNullFunction<S, S>
    miscData(ProviderType<? extends D> providerType, NonNullConsumer<? extends D> action) {
        Objects.requireNonNull(providerType, "providerType");
        Objects.requireNonNull(action, "action");
        return builder -> builder.addMiscData(providerType, action);
    }
}
