package org.pickaid.pibrary.api.registrate;

import com.mojang.serialization.Codec;
import com.tterrag.registrate.AbstractRegistrate;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.blockentity.PiStateBlockEntity;
import org.pickaid.pibrary.api.registrate.config.PiConfigRegistration;
import org.pickaid.pibrary.api.registrate.config.PiCreativeTabRegistration;
import org.pickaid.pibrary.api.registrate.host.PiBlockEntityStateEntry;
import org.pickaid.pibrary.api.registrate.host.PiChunkServiceEntry;
import org.pickaid.pibrary.api.registrate.host.PiEntityServiceEntry;
import org.pickaid.pibrary.api.registrate.host.PiLevelServiceEntry;
import org.pickaid.pibrary.api.registrate.host.PiPlayerServiceEntry;
import org.pickaid.pibrary.api.registrate.registry.PiCustomRegistryBuilder;
import org.pickaid.pibrary.api.registrate.registry.PiDatapackRegistryBuilder;
import org.pickaid.pibrary.api.service.PiChunkServiceContext;
import org.pickaid.pibrary.api.service.PiStateChunkService;
import org.pickaid.pibrary.api.service.PiLevelServiceContext;
import org.pickaid.pibrary.api.service.PiLivingServiceContext;
import org.pickaid.pibrary.api.service.PiStateLevelService;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;

public final class PiRegistrate extends AbstractRegistrate<PiRegistrate> {
    private final PiRegistrateContext context;
    private final @Nullable IEventBus explicitModEventBus;

    private PiRegistrate(String modId) {
        this(modId, null);
    }

    private PiRegistrate(String modId, @Nullable IEventBus explicitModEventBus) {
        super(modId);
        this.explicitModEventBus = explicitModEventBus;
        this.context = new PiRegistrateContext(modId);
    }

    public static PiRegistrate create(String modId) {
        if (modId == null || modId.isBlank()) {
            throw new IllegalArgumentException("modId must not be blank");
        }

        PiRegistrate registrate = new PiRegistrate(modId);
        registrate.registerEventListeners(registrate.getModEventBus());
        return registrate;
    }

    static PiRegistrate create(String modId, IEventBus modEventBus) {
        if (modId == null || modId.isBlank()) {
            throw new IllegalArgumentException("modId must not be blank");
        }

        PiRegistrate registrate = new PiRegistrate(modId, modEventBus);
        registrate.registerEventListeners(registrate.getModEventBus());
        return registrate;
    }

    @Override
    protected PiRegistrate registerEventListeners(IEventBus bus) {
        if (explicitModEventBus != null && bus == explicitModEventBus) {
            bus.addListener(EventPriority.NORMAL, false, RegisterEvent.class, this::onRegister);
            bus.addListener(EventPriority.LOWEST, false, RegisterEvent.class, this::onRegisterLate);
            bus.addListener(EventPriority.NORMAL, false, BuildCreativeModeTabContentsEvent.class, this::onBuildCreativeModeTabContents);
            bus.addListener(EventPriority.NORMAL, false, FMLCommonSetupEvent.class, event -> {});
            return this;
        }

        return super.registerEventListeners(bus);
    }

    @Override
    public IEventBus getModEventBus() {
        return explicitModEventBus != null ? explicitModEventBus : super.getModEventBus();
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

    public <T> PiConfigRegistration<T> config(String path, Codec<T> codec, T defaultValue) {
        return new PiConfigRegistration<>(this, path, codec, defaultValue);
    }

    public PiCreativeTabRegistration creativeTab(String path, String title) {
        return new PiCreativeTabRegistration(this, path, title);
    }

    public <S, T extends PiStateLivingEntityService<S>> PiEntityServiceEntry<S, T> entityService(
            String path,
            Class<S> stateType,
            Function<PiLivingServiceContext, T> factory
    ) {
        return new PiEntityServiceEntry<>(this, path, stateType, factory);
    }

    public <S, T extends PiStateLivingEntityService<S>> PiPlayerServiceEntry<S, T> playerService(
            String path,
            Class<S> stateType,
            Function<PiLivingServiceContext, T> factory
    ) {
        return new PiPlayerServiceEntry<>(this, path, stateType, factory);
    }

    public <S, T extends PiStateChunkService<S>> PiChunkServiceEntry<S, T> chunkService(
            String path,
            Class<S> stateType,
            Function<PiChunkServiceContext, T> factory
    ) {
        return new PiChunkServiceEntry<>(this, path, stateType, factory);
    }

    public <S, T extends PiStateLevelService<S>> PiLevelServiceEntry<S, T> levelService(
            String path,
            Class<S> stateType,
            Function<PiLevelServiceContext, T> factory
    ) {
        return new PiLevelServiceEntry<>(this, path, stateType, factory);
    }

    public <S, T extends PiStateBlockEntity<S>> PiBlockEntityStateEntry<S, T> blockEntityService(
            String path,
            Class<S> stateType,
            Class<T> blockEntityType
    ) {
        return new PiBlockEntityStateEntry<>(this, path, stateType, blockEntityType);
    }
}
