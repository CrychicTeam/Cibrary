package org.pickaid.pibrary.api.creative;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Collects creative-tab contents before Minecraft asks a tab to build entries.
 */
public final class PiCreativeContentRegistry {
    private final Map<ResourceKey<CreativeModeTab>, LinkedHashMap<String, List<Request>>> byTab =
            new LinkedHashMap<>();
    private final Map<ResourceKey<CreativeModeTab>, List<String>> sectionOrders = new LinkedHashMap<>();

    public PiCreativeContentRegistry order(ResourceKey<CreativeModeTab> tab, String... sections) {
        Objects.requireNonNull(sections, "sections");
        return order(tab, List.of(sections));
    }

    public PiCreativeContentRegistry order(ResourceKey<CreativeModeTab> tab, List<String> sections) {
        Objects.requireNonNull(tab, "tab");
        Objects.requireNonNull(sections, "sections");
        List<String> cleanSections = new ArrayList<>(sections.size());
        Set<String> seen = new LinkedHashSet<>();
        for (String section : sections) {
            String cleanSection = requireSection(section);
            if (!seen.add(cleanSection)) {
                throw new IllegalArgumentException("duplicate creative section: " + cleanSection);
            }
            cleanSections.add(cleanSection);
        }
        sectionOrders.put(tab, List.copyOf(cleanSections));
        return this;
    }

    public List<String> sectionOrder(ResourceKey<CreativeModeTab> tab) {
        Objects.requireNonNull(tab, "tab");
        return sectionOrders.getOrDefault(tab, List.of());
    }

    public PiCreativeContentRegistry addStack(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<ItemStack> stack
    ) {
        return addStack(tab, section, stack, PiCreativeVisibility.PARENT_AND_SEARCH);
    }

    public PiCreativeContentRegistry addStack(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<ItemStack> stack,
            PiCreativeVisibility visibility
    ) {
        return add(tab, section, stack, visibility);
    }

    public PiCreativeContentRegistry addStackVariant(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<ItemStack> stack,
            String variantId,
            Consumer<ItemStack> configure
    ) {
        return addStackVariant(tab, section, stack, variantId, configure, PiCreativeVisibility.PARENT_AND_SEARCH);
    }

    public PiCreativeContentRegistry addStackVariant(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<ItemStack> stack,
            String variantId,
            Consumer<ItemStack> configure,
            PiCreativeVisibility visibility
    ) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(variantId, "variantId");
        Objects.requireNonNull(configure, "configure");
        Objects.requireNonNull(visibility, "visibility");
        return add(tab, section, variantId, () -> {
            ItemStack copy = Objects.requireNonNull(stack.get(), "stack.get()").copy();
            configure.accept(copy);
            return copy;
        }, visibility);
    }

    public <V> PiCreativeContentRegistry addStackVariants(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<ItemStack> stack,
            Iterable<V> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure
    ) {
        return addStackVariants(tab, section, stack, values, variantId, configure, PiCreativeVisibility.PARENT_AND_SEARCH);
    }

    public <V> PiCreativeContentRegistry addStackVariants(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<ItemStack> stack,
            Iterable<V> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure,
            PiCreativeVisibility visibility
    ) {
        Objects.requireNonNull(values, "values");
        return addStackVariants(tab, section, stack, () -> values, variantId, configure, visibility);
    }

    public <V> PiCreativeContentRegistry addStackVariants(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<ItemStack> stack,
            Supplier<? extends Iterable<V>> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure
    ) {
        return addStackVariants(tab, section, stack, values, variantId, configure, PiCreativeVisibility.PARENT_AND_SEARCH);
    }

    public <V> PiCreativeContentRegistry addStackVariants(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<ItemStack> stack,
            Supplier<? extends Iterable<V>> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure,
            PiCreativeVisibility visibility
    ) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(variantId, "variantId");
        Objects.requireNonNull(configure, "configure");
        Objects.requireNonNull(visibility, "visibility");
        return add(tab, section, new VariantRequest<>(stack, values, variantId, configure, visibility));
    }

    public PiCreativeContentRegistry addItem(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<? extends ItemLike> item
    ) {
        return addItem(tab, section, item, PiCreativeVisibility.PARENT_AND_SEARCH);
    }

    public PiCreativeContentRegistry addItem(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<? extends ItemLike> item,
            PiCreativeVisibility visibility
    ) {
        Objects.requireNonNull(item, "item");
        return add(tab, section, () -> item.get().asItem().getDefaultInstance(), visibility);
    }

    public PiCreativeContentRegistry addVariant(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<? extends ItemLike> item,
            String variantId,
            Consumer<ItemStack> configure
    ) {
        return addVariant(tab, section, item, variantId, configure, PiCreativeVisibility.PARENT_AND_SEARCH);
    }

    public PiCreativeContentRegistry addVariant(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<? extends ItemLike> item,
            String variantId,
            Consumer<ItemStack> configure,
            PiCreativeVisibility visibility
    ) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(variantId, "variantId");
        Objects.requireNonNull(configure, "configure");
        Objects.requireNonNull(visibility, "visibility");
        return addStackVariant(tab, section, () -> item.get().asItem().getDefaultInstance(), variantId, configure, visibility);
    }

    public <V> PiCreativeContentRegistry addVariants(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<? extends ItemLike> item,
            Iterable<V> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure
    ) {
        return addVariants(tab, section, item, values, variantId, configure, PiCreativeVisibility.PARENT_AND_SEARCH);
    }

    public <V> PiCreativeContentRegistry addVariants(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<? extends ItemLike> item,
            Iterable<V> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure,
            PiCreativeVisibility visibility
    ) {
        Objects.requireNonNull(values, "values");
        return addVariants(tab, section, item, () -> values, variantId, configure, visibility);
    }

    public <V> PiCreativeContentRegistry addVariants(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<? extends ItemLike> item,
            Supplier<? extends Iterable<V>> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure
    ) {
        return addVariants(tab, section, item, values, variantId, configure, PiCreativeVisibility.PARENT_AND_SEARCH);
    }

    public <V> PiCreativeContentRegistry addVariants(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<? extends ItemLike> item,
            Supplier<? extends Iterable<V>> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure,
            PiCreativeVisibility visibility
    ) {
        Objects.requireNonNull(item, "item");
        return addStackVariants(
                tab,
                section,
                () -> item.get().asItem().getDefaultInstance(),
                values,
                variantId,
                configure,
                visibility);
    }

    public List<PiCreativeEntry> collect(ResourceKey<CreativeModeTab> tab, List<String> sectionOrder) {
        List<PiCreativeEntry> entries = new ArrayList<>();
        for (PiCreativeEntryPlan plan : plans(tab, sectionOrder)) {
            entries.add(new PiCreativeEntry(plan.section(), plan.stack().get(), plan.visibility()));
        }
        return List.copyOf(entries);
    }

    public List<PiCreativeEntryPlan> plans(ResourceKey<CreativeModeTab> tab) {
        return plans(tab, sectionOrder(tab));
    }

    public List<PiCreativeEntryPlan> plans(ResourceKey<CreativeModeTab> tab, List<String> sectionOrder) {
        Objects.requireNonNull(tab, "tab");
        Objects.requireNonNull(sectionOrder, "sectionOrder");
        LinkedHashMap<String, List<Request>> sections = byTab.get(tab);
        if (sections == null || sections.isEmpty()) {
            return List.of();
        }
        List<PiCreativeEntryPlan> entries = new ArrayList<>();
        Set<String> emitted = new LinkedHashSet<>();
        for (String section : sectionOrder) {
            emitSection(entries, sections, requireSection(section), emitted);
        }
        for (String section : sections.keySet()) {
            emitSection(entries, sections, section, emitted);
        }
        return List.copyOf(entries);
    }

    public int emit(ResourceKey<CreativeModeTab> tab, PiCreativeOutput output) {
        Objects.requireNonNull(output, "output");
        List<PiCreativeEntryPlan> entries = plans(tab);
        for (PiCreativeEntryPlan entry : entries) {
            output.accept(entry.stack(), entry.visibility());
        }
        return entries.size();
    }

    private PiCreativeContentRegistry add(
            ResourceKey<CreativeModeTab> tab,
            String section,
            Supplier<ItemStack> stack,
            PiCreativeVisibility visibility
    ) {
        return add(tab, section, "default", stack, visibility);
    }

    private PiCreativeContentRegistry add(
            ResourceKey<CreativeModeTab> tab,
            String section,
            String id,
            Supplier<ItemStack> stack,
            PiCreativeVisibility visibility
    ) {
        return add(tab, section, new SingleRequest(id, stack, visibility));
    }

    private PiCreativeContentRegistry add(ResourceKey<CreativeModeTab> tab, String section, Request request) {
        Objects.requireNonNull(tab, "tab");
        Objects.requireNonNull(request, "request");
        String cleanSection = requireSection(section);
        byTab.computeIfAbsent(tab, ignored -> new LinkedHashMap<>())
                .computeIfAbsent(cleanSection, ignored -> new ArrayList<>())
                .add(request);
        return this;
    }

    private static void emitSection(
            List<PiCreativeEntryPlan> entries,
            Map<String, List<Request>> sections,
            String section,
            Set<String> emitted
    ) {
        if (!emitted.add(section)) {
            return;
        }
        List<Request> requests = sections.get(section);
        if (requests == null) {
            return;
        }
        for (Request request : requests) {
            request.emit(section, entries);
        }
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

    private interface Request {
        void emit(String section, List<PiCreativeEntryPlan> entries);
    }

    private record SingleRequest(
            String id,
            Supplier<ItemStack> stack,
            PiCreativeVisibility visibility
    ) implements Request {
        private SingleRequest {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(stack, "stack");
            Objects.requireNonNull(visibility, "visibility");
        }

        @Override
        public void emit(String section, List<PiCreativeEntryPlan> entries) {
            if (visibility != PiCreativeVisibility.HIDDEN) {
                entries.add(new PiCreativeEntryPlan(section, id, stack, visibility));
            }
        }
    }

    private record VariantRequest<V>(
            Supplier<ItemStack> stack,
            Supplier<? extends Iterable<V>> values,
            Function<? super V, String> variantId,
            BiConsumer<ItemStack, ? super V> configure,
            PiCreativeVisibility visibility
    ) implements Request {
        private VariantRequest {
            Objects.requireNonNull(stack, "stack");
            Objects.requireNonNull(values, "values");
            Objects.requireNonNull(variantId, "variantId");
            Objects.requireNonNull(configure, "configure");
            Objects.requireNonNull(visibility, "visibility");
        }

        @Override
        public void emit(String section, List<PiCreativeEntryPlan> entries) {
            if (visibility == PiCreativeVisibility.HIDDEN) {
                return;
            }
            Iterable<V> generatedValues = Objects.requireNonNull(values.get(), "values.get()");
            for (V value : generatedValues) {
                V variantValue = Objects.requireNonNull(value, "variant value");
                String id = requireVariantId(variantId.apply(variantValue));
                entries.add(new PiCreativeEntryPlan(section, id, () -> {
                    ItemStack copy = Objects.requireNonNull(stack.get(), "stack.get()").copy();
                    configure.accept(copy, variantValue);
                    return copy;
                }, visibility));
            }
        }
    }
}
