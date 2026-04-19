package org.pickaid.pibrary.api.registrate;

import com.mojang.serialization.Codec;
import com.tterrag.registrate.AbstractRegistrate;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.api.registrate.host.PiLevelServiceEntry;
import org.pickaid.pibrary.api.registrate.registry.PiCustomRegistryBuilder;
import org.pickaid.pibrary.api.registrate.registry.PiDatapackRegistryBuilder;
import org.pickaid.pibrary.api.service.PiLevelServiceContext;

public final class PiRegistrate extends AbstractRegistrate<PiRegistrate> {
    private final PiRegistrateContext context;

    private PiRegistrate(String modId) {
        super(modId);
        this.context = new PiRegistrateContext(modId);
    }

    public static PiRegistrate create(String modId) {
        return new PiRegistrate(modId);
    }

    public PiRegistrateContext context() {
        return context;
    }

    public ResourceLocation id(String path) {
        return PiRegistrateDefaults.id(getModid(), path);
    }

    public <T> PiCustomRegistryBuilder<T> customRegistry(String path, Class<T> valueType) {
        return new PiCustomRegistryBuilder<>(this, path, valueType);
    }

    public <T> PiDatapackRegistryBuilder<T> datapackRegistry(String path, Codec<T> directCodec, Codec<T> networkCodec) {
        return new PiDatapackRegistryBuilder<>(this, path, directCodec, networkCodec);
    }

    public <S, T> PiLevelServiceEntry<S, T> levelService(String path, Class<S> stateType, Function<PiLevelServiceContext, T> factory) {
        return new PiLevelServiceEntry<>(this, path, stateType, factory);
    }
}
