package org.pickaid.pibrary.api.registrate;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import org.pickaid.pibrary.api.creative.PiCreativeContentRegistry;
import org.pickaid.pibrary.api.creative.PiCreativeTabBuilder;
import org.pickaid.pibrary.api.render.tint.PiTintRegistry;
import org.pickaid.pibrary.api.util.PiIds;
import org.pickaid.pibrary.runtime.creative.PiForgeCreativeTabs;

/**
 * Base class for mods that want a typed Registrate entry point.
 *
 * <p>The important part is the self type: a mod can extend
 * {@code PiBaseRegistrate<MyRegistrate>} and keep Registrate's fluent return
 * types while adding its own domain methods such as {@code spell(...)},
 * {@code trait(...)}, or {@code machine(...)}. Pibrary only provides common id
 * helpers here; project-specific builders should live in the project that owns
 * the gameplay concept.</p>
 *
 * @param <S> concrete registrate type
 */
public abstract class PiBaseRegistrate<S extends PiBaseRegistrate<S>>
        extends AbstractRegistrate<S>
        implements PiTintedRegistrate<S> {
    private final PiTintRegistry tintRegistry = new PiTintRegistry();
    private final PiCreativeContentRegistry creativeContents = new PiCreativeContentRegistry();
    private ResourceKey<CreativeModeTab> defaultCreativeTabKey;
    private boolean creativeContentsAttached;

    protected PiBaseRegistrate(String modid) {
        super(modid);
    }

    /**
     * Registers this Registrate instance to an explicit mod event bus.
     *
     * @param modBus Forge mod event bus
     * @return this Registrate instance
     */
    protected final S registerTo(IEventBus modBus) {
        IEventBus bus = Objects.requireNonNull(modBus, "modBus");
        registerEventListeners(bus);
        tintRegistry.registerTo(bus);
        if (!creativeContentsAttached) {
            creativeContentsAttached = true;
            PiForgeCreativeTabs.registerTo(bus, creativeContents);
        }
        return self();
    }

    /**
     * Registers this Registrate instance to Registrate's default mod event bus.
     *
     * @return this Registrate instance
     */
    protected final S registerToDefaultBus() {
        return registerTo(getModEventBus());
    }

    public ResourceLocation loc(String path) {
        return PiIds.id(getModid(), path);
    }

    public ResourceLocation blockTexture(String path) {
        return PiIds.blockTexture(getModid(), path);
    }

    public ResourceLocation itemTexture(String path) {
        return PiIds.itemTexture(getModid(), path);
    }

    public <T> ResourceKey<Registry<T>> registryKey(String path) {
        return ResourceKey.createRegistryKey(loc(path));
    }

    @Override
    public S defaultCreativeTab(ResourceKey<CreativeModeTab> tab) {
        defaultCreativeTabKey = tab;
        return super.defaultCreativeTab(tab);
    }

    public final PiCreativeContentRegistry creativeContents() {
        return creativeContents;
    }

    public final S creativeSections(String... sections) {
        creativeContents.order(requireDefaultCreativeTab(), sections);
        return self();
    }

    public final S creativeSections(ResourceKey<CreativeModeTab> tab, String... sections) {
        creativeContents.order(tab, sections);
        return self();
    }

    public final NoConfigBuilder<CreativeModeTab, CreativeModeTab, S> creativeTab(
            String name,
            Consumer<PiCreativeTabBuilder> configure
    ) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(configure, "configure");
        ResourceKey<CreativeModeTab> tab = ResourceKey.create(Registries.CREATIVE_MODE_TAB, loc(name));
        defaultCreativeTabKey = tab;
        return defaultCreativeTab(name, builder ->
                configure.accept(new PiCreativeTabBuilder(tab, builder, creativeContents)));
    }

    public final ResourceKey<CreativeModeTab> requireDefaultCreativeTab() {
        if (defaultCreativeTabKey == null) {
            throw new IllegalStateException("default creative tab must be set before using section(String)");
        }
        return defaultCreativeTabKey;
    }

    @Override
    public <T extends Item> PiItemBuilder<T, S> item(NonNullFunction<Item.Properties, T> factory) {
        return item(currentName(), factory);
    }

    @Override
    public <T extends Item> PiItemBuilder<T, S> item(
            String name,
            NonNullFunction<Item.Properties, T> factory
    ) {
        return item(self(), name, factory);
    }

    @Override
    public <T extends Item, P> PiItemBuilder<T, P> item(
            P parent,
            NonNullFunction<Item.Properties, T> factory
    ) {
        return item(parent, currentName(), factory);
    }

    @Override
    public <T extends Item, P> PiItemBuilder<T, P> item(
            P parent,
            String name,
            NonNullFunction<Item.Properties, T> factory
    ) {
        @SuppressWarnings("unchecked")
        PiItemBuilder<T, P> builder = (PiItemBuilder<T, P>) (Object)
                this.<Item, T, P, ItemBuilder<T, P>>entry(name, callback -> PiItemBuilder.create(
                this,
                parent,
                name,
                callback,
                factory,
                creativeContents,
                this::currentDefaultCreativeTab));
        return builder;
    }

    @Override
    public <T extends Block> PiBlockBuilder<T, S> block(NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(currentName(), factory);
    }

    @Override
    public <T extends Block> PiBlockBuilder<T, S> block(
            String name,
            NonNullFunction<BlockBehaviour.Properties, T> factory
    ) {
        return block(self(), name, factory);
    }

    @Override
    public <T extends Block, P> PiBlockBuilder<T, P> block(
            P parent,
            NonNullFunction<BlockBehaviour.Properties, T> factory
    ) {
        return block(parent, currentName(), factory);
    }

    @Override
    public <T extends Block, P> PiBlockBuilder<T, P> block(
            P parent,
            String name,
            NonNullFunction<BlockBehaviour.Properties, T> factory
    ) {
        @SuppressWarnings("unchecked")
        PiBlockBuilder<T, P> builder = (PiBlockBuilder<T, P>) (Object)
                this.<Block, T, P, BlockBuilder<T, P>>entry(name, callback -> PiBlockBuilder.create(
                this,
                parent,
                name,
                callback,
                factory,
                creativeContents,
                this::currentDefaultCreativeTab));
        return builder;
    }

    /**
     * Returns this registrate's tint registry.
     *
     * <p>Use this for block and item model tint declarations that should be
     * consumed by the same mod event bus as the rest of this registrate.</p>
     *
     * @return tint registry
     */
    @Override
    public final PiTintRegistry tints() {
        return tintRegistry;
    }

    private ResourceKey<CreativeModeTab> currentDefaultCreativeTab() {
        return defaultCreativeTabKey;
    }
}
