package org.pickaid.pibrary.api.registrate;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import org.pickaid.pibrary.api.creative.PiCreativeContentRegistry;
import org.pickaid.pibrary.api.creative.PiCreativeVisibility;

/**
 * Registrate item builder with Pibrary creative-tab shortcuts.
 */
public class PiItemBuilder<T extends Item, P> extends ItemBuilder<T, P> {
    private final PiCreativeContentRegistry creativeContents;
    private final Supplier<ResourceKey<CreativeModeTab>> defaultTab;
    private final List<Variant> variants = new ArrayList<>();
    private final List<VariantGroup<?>> variantGroups = new ArrayList<>();
    private Supplier<ResourceKey<CreativeModeTab>> tab;
    private String section;
    private PiCreativeVisibility visibility = PiCreativeVisibility.PARENT_AND_SEARCH;
    private boolean creativeHookInstalled;

    public static <T extends Item, P> PiItemBuilder<T, P> create(
            AbstractRegistrate<?> owner,
            P parent,
            String name,
            BuilderCallback callback,
            NonNullFunction<Item.Properties, T> factory,
            PiCreativeContentRegistry creativeContents,
            Supplier<ResourceKey<CreativeModeTab>> defaultTab
    ) {
        PiItemBuilder<T, P> builder = new PiItemBuilder<>(
                owner,
                parent,
                name,
                callback,
                factory,
                creativeContents,
                defaultTab);
        builder.defaultModel();
        builder.defaultLang();
        return builder;
    }

    protected PiItemBuilder(
            AbstractRegistrate<?> owner,
            P parent,
            String name,
            BuilderCallback callback,
            NonNullFunction<Item.Properties, T> factory,
            PiCreativeContentRegistry creativeContents,
            Supplier<ResourceKey<CreativeModeTab>> defaultTab
    ) {
        super(owner, parent, name, callback, factory);
        this.creativeContents = Objects.requireNonNull(creativeContents, "creativeContents");
        this.defaultTab = Objects.requireNonNull(defaultTab, "defaultTab");
    }

    public PiItemBuilder<T, P> section(String section) {
        this.tab = PiCreativeTabTarget.defaultTab(defaultTab);
        this.section = requireSection(section);
        return this;
    }

    public PiItemBuilder<T, P> section(ResourceKey<CreativeModeTab> tab, String section) {
        this.tab = PiCreativeTabTarget.explicit(tab);
        this.section = requireSection(section);
        return this;
    }

    public PiItemBuilder<T, P> itemTag(TagKey<Item> tag) {
        return itemTags(tag);
    }

    @SafeVarargs
    public final PiItemBuilder<T, P> itemTags(TagKey<Item>... tags) {
        tag(copyItemTags(tags));
        return this;
    }

    public PiItemBuilder<T, P> stacksTo(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("stack size must be positive");
        }
        return properties(properties -> properties.stacksTo(count));
    }

    public PiItemBuilder<T, P> durability(int durability) {
        if (durability <= 0) {
            throw new IllegalArgumentException("durability must be positive");
        }
        return properties(properties -> properties.durability(durability));
    }

    public PiItemBuilder<T, P> fireResistant() {
        return properties(Item.Properties::fireResistant);
    }

    public PiItemBuilder<T, P> rarity(Rarity rarity) {
        Objects.requireNonNull(rarity, "rarity");
        return properties(properties -> properties.rarity(rarity));
    }

    @Override
    public PiItemBuilder<T, P> properties(NonNullUnaryOperator<Item.Properties> func) {
        super.properties(func);
        return this;
    }

    @Override
    public PiItemBuilder<T, P> initialProperties(NonNullSupplier<Item.Properties> properties) {
        super.initialProperties(properties);
        return this;
    }

    @Override
    public PiItemBuilder<T, P> defaultModel() {
        super.defaultModel();
        return this;
    }

    @Override
    public PiItemBuilder<T, P> defaultLang() {
        super.defaultLang();
        return this;
    }

    public PiItemBuilder<T, P> variant(String id, Consumer<ItemStack> configure) {
        variants.add(new Variant(requireVariantId(id), Objects.requireNonNull(configure, "configure")));
        return this;
    }

    public <V> PiItemBuilder<T, P> variants(
            Iterable<V> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure
    ) {
        List<V> copiedValues = copyValues(values);
        return variants(() -> copiedValues, variantId, configure);
    }

    public <V> PiItemBuilder<T, P> variants(
            Supplier<? extends Iterable<V>> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure
    ) {
        variantGroups.add(new VariantGroup<>(
                Objects.requireNonNull(values, "values"),
                Objects.requireNonNull(variantId, "variantId"),
                Objects.requireNonNull(configure, "configure")));
        return this;
    }

    public PiItemBuilder<T, P> searchOnly() {
        visibility = PiCreativeVisibility.SEARCH_ONLY;
        return this;
    }

    public PiItemBuilder<T, P> parentOnly() {
        visibility = PiCreativeVisibility.PARENT_ONLY;
        return this;
    }

    public PiItemBuilder<T, P> hidden() {
        visibility = PiCreativeVisibility.HIDDEN;
        return this;
    }

    @Override
    public ItemEntry<T> register() {
        installCreativeHook();
        return super.register();
    }

    private void installCreativeHook() {
        if (creativeHookInstalled || section == null) {
            return;
        }
        creativeHookInstalled = true;
        Supplier<ResourceKey<CreativeModeTab>> targetTab = tab;
        String targetSection = section;
        PiCreativeVisibility targetVisibility = visibility;
        List<Variant> targetVariants = List.copyOf(variants);
        List<VariantGroup<?>> targetVariantGroups = List.copyOf(variantGroups);
        onRegister(item -> {
            creativeContents.addItem(targetTab.get(), targetSection, () -> item, targetVisibility);
            for (Variant variant : targetVariants) {
                creativeContents.addVariant(targetTab.get(), targetSection, () -> item, variant.id(), variant.configure(), targetVisibility);
            }
            for (VariantGroup<?> group : targetVariantGroups) {
                addVariantGroup(creativeContents, targetTab.get(), targetSection, () -> item, group, targetVisibility);
            }
        });
    }

    private static <V> void addVariantGroup(
            PiCreativeContentRegistry creativeContents,
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<? extends ItemLike> item,
            VariantGroup<V> group,
            PiCreativeVisibility visibility
    ) {
        creativeContents.addVariants(tab, section, item, group.values(), group.variantId(), group.configure(), visibility);
    }

    private static String requireSection(String section) {
        Objects.requireNonNull(section, "section");
        if (section.isBlank()) {
            throw new IllegalArgumentException("section must not be blank");
        }
        return section;
    }

    private static String requireVariantId(String id) {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) {
            throw new IllegalArgumentException("variant id must not be blank");
        }
        return id;
    }

    private static <V> List<V> copyValues(Iterable<V> values) {
        Objects.requireNonNull(values, "values");
        List<V> copy = new ArrayList<>();
        for (V value : values) {
            copy.add(Objects.requireNonNull(value, "variant value"));
        }
        return List.copyOf(copy);
    }

    private static TagKey<Item>[] copyItemTags(TagKey<Item>[] tags) {
        Objects.requireNonNull(tags, "tags");
        TagKey<Item>[] copy = Arrays.copyOf(tags, tags.length);
        for (int i = 0; i < copy.length; i++) {
            Objects.requireNonNull(copy[i], "tags[" + i + "]");
        }
        return copy;
    }

    private record Variant(String id, Consumer<ItemStack> configure) {
    }

    private record VariantGroup<V>(
            Supplier<? extends Iterable<V>> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure
    ) {
    }
}
