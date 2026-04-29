package org.pickaid.pibrary.api.render.tint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Tint declarations owned by a Pibrary registrate instance.
 *
 * <p>{@code PiBaseRegistrate} owns one registry, attaches it to the mod event
 * bus, and consumes declarations from here during Minecraft's normal client
 * color events.</p>
 */
public final class PiTintRegistry {
    private final List<BlockRegistration> blockRegistrations = new ArrayList<>();
    private final List<ItemRegistration> itemRegistrations = new ArrayList<>();
    private boolean attached;

    public PiTintRegistry() {
    }

    @SafeVarargs
    public final PiTintRegistry block(BlockColor color, Supplier<? extends Block>... blocks) {
        blockRegistrations.add(new BlockRegistration(
                Objects.requireNonNull(color, "color"),
                copySuppliers(blocks, "blocks")
        ));
        return this;
    }

    public PiTintRegistry blockValue(BlockColor color, Block block) {
        Objects.requireNonNull(block, "block");
        return block(color, () -> block);
    }

    @SafeVarargs
    public final PiTintRegistry blockConstant(int color, Supplier<? extends Block>... blocks) {
        return block(PiBlockTints.layer(0, color), blocks);
    }

    @SafeVarargs
    public final PiTintRegistry blockGrass(Supplier<? extends Block>... blocks) {
        return block(PiBlockTints.layer(0, PiBlockTints.grass()), blocks);
    }

    @SafeVarargs
    public final PiTintRegistry blockFoliage(Supplier<? extends Block>... blocks) {
        return block(PiBlockTints.layer(0, PiBlockTints.foliage()), blocks);
    }

    @SafeVarargs
    public final PiTintRegistry blockWater(Supplier<? extends Block>... blocks) {
        return block(PiBlockTints.layer(0, PiBlockTints.water()), blocks);
    }

    @SafeVarargs
    public final PiTintRegistry item(ItemColor color, Supplier<? extends ItemLike>... items) {
        itemRegistrations.add(new ItemRegistration(
                Objects.requireNonNull(color, "color"),
                copySuppliers(items, "items")
        ));
        return this;
    }

    public PiTintRegistry itemValue(ItemColor color, ItemLike item) {
        Objects.requireNonNull(item, "item");
        return item(color, () -> item);
    }

    @SafeVarargs
    public final PiTintRegistry itemConstant(int layer, int color, Supplier<? extends ItemLike>... items) {
        return item(PiItemTints.layer(layer, color), items);
    }

    @SafeVarargs
    public final PiTintRegistry itemDurability(
            int layer,
            int brokenColor,
            int fullColor,
            Supplier<? extends ItemLike>... items
    ) {
        return item(PiItemTints.durabilityLayer(layer, brokenColor, fullColor), items);
    }

    @SafeVarargs
    public final PiTintRegistry itemFromBlock(Supplier<? extends Block>... blocks) {
        return itemFromBlockLayer(0, blocks);
    }

    @SafeVarargs
    public final PiTintRegistry itemFromBlockLayer(int layer, Supplier<? extends Block>... blocks) {
        List<Supplier<? extends ItemLike>> items = new ArrayList<>();
        for (Supplier<? extends Block> block : copySuppliers(blocks, "blocks")) {
            items.add(block);
        }
        itemRegistrations.add(new ItemRegistration(PiItemTints.blockItemLayer(layer), List.copyOf(items)));
        return this;
    }

    @SafeVarargs
    public final PiTintRegistry blockAndItem(
            BlockColor blockColor,
            ItemColor itemColor,
            Supplier<? extends Block>... blocks
    ) {
        List<Supplier<? extends Block>> blockSuppliers = copySuppliers(blocks, "blocks");
        blockRegistrations.add(new BlockRegistration(Objects.requireNonNull(blockColor, "blockColor"), blockSuppliers));
        List<Supplier<? extends ItemLike>> items = new ArrayList<>(blockSuppliers.size());
        items.addAll(blockSuppliers);
        itemRegistrations.add(new ItemRegistration(Objects.requireNonNull(itemColor, "itemColor"), List.copyOf(items)));
        return this;
    }

    /**
     * Attaches this registry to Forge's client color events.
     *
     * @param modBus mod event bus
     * @return this registry
     */
    public PiTintRegistry registerTo(IEventBus modBus) {
        Objects.requireNonNull(modBus, "modBus");
        if (!attached) {
            attached = true;
            modBus.addListener(this::registerBlocks);
            modBus.addListener(this::registerItems);
        }
        return this;
    }

    /**
     * Resolves all targets and fails if one block or item is registered twice.
     *
     * @return this registry
     */
    public PiTintRegistry validate() {
        resolveBlocks();
        resolveItems();
        return this;
    }

    public int registerBlocks(RegisterColorHandlersEvent.Block event) {
        Objects.requireNonNull(event, "event");
        List<ResolvedBlockRegistration> registrations = resolveBlocks();
        for (ResolvedBlockRegistration registration : registrations) {
            event.register(registration.color(), registration.blocks().toArray(Block[]::new));
        }
        return registrations.stream().mapToInt(registration -> registration.blocks().size()).sum();
    }

    public int registerItems(RegisterColorHandlersEvent.Item event) {
        Objects.requireNonNull(event, "event");
        List<ResolvedItemRegistration> registrations = resolveItems();
        for (ResolvedItemRegistration registration : registrations) {
            event.register(registration.color(), registration.items().toArray(ItemLike[]::new));
        }
        return registrations.stream().mapToInt(registration -> registration.items().size()).sum();
    }

    public int blockTargetCount() {
        return blockRegistrations.stream().mapToInt(registration -> registration.blocks().size()).sum();
    }

    public int itemTargetCount() {
        return itemRegistrations.stream().mapToInt(registration -> registration.items().size()).sum();
    }

    private List<ResolvedBlockRegistration> resolveBlocks() {
        Set<Block> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<ResolvedBlockRegistration> result = new ArrayList<>(blockRegistrations.size());
        for (BlockRegistration registration : blockRegistrations) {
            List<Block> blocks = new ArrayList<>(registration.blocks().size());
            for (Supplier<? extends Block> supplier : registration.blocks()) {
                Block block = Objects.requireNonNull(supplier.get(), "block supplier returned null");
                if (!seen.add(block)) {
                    throw new IllegalStateException("Duplicate block tint registration for " + block);
                }
                blocks.add(block);
            }
            result.add(new ResolvedBlockRegistration(registration.color(), List.copyOf(blocks)));
        }
        return List.copyOf(result);
    }

    private List<ResolvedItemRegistration> resolveItems() {
        Set<net.minecraft.world.item.Item> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        List<ResolvedItemRegistration> result = new ArrayList<>(itemRegistrations.size());
        for (ItemRegistration registration : itemRegistrations) {
            List<ItemLike> items = new ArrayList<>(registration.items().size());
            for (Supplier<? extends ItemLike> supplier : registration.items()) {
                ItemLike item = Objects.requireNonNull(supplier.get(), "item supplier returned null");
                if (!seen.add(item.asItem())) {
                    throw new IllegalStateException("Duplicate item tint registration for " + item.asItem());
                }
                items.add(item);
            }
            result.add(new ResolvedItemRegistration(registration.color(), List.copyOf(items)));
        }
        return List.copyOf(result);
    }

    private static <T> List<Supplier<? extends T>> copySuppliers(Supplier<? extends T>[] suppliers, String name) {
        Objects.requireNonNull(suppliers, name);
        List<Supplier<? extends T>> copy = new ArrayList<>(suppliers.length);
        for (int i = 0; i < suppliers.length; i++) {
            copy.add(Objects.requireNonNull(suppliers[i], name + "[" + i + "]"));
        }
        return List.copyOf(copy);
    }

    private record BlockRegistration(BlockColor color, List<Supplier<? extends Block>> blocks) {
    }

    private record ItemRegistration(ItemColor color, List<Supplier<? extends ItemLike>> items) {
    }

    private record ResolvedBlockRegistration(BlockColor color, List<Block> blocks) {
    }

    private record ResolvedItemRegistration(ItemColor color, List<ItemLike> items) {
    }
}
