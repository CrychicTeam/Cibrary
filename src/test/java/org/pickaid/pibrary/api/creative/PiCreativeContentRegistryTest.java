package org.pickaid.pibrary.api.creative;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

class PiCreativeContentRegistryTest {
    private static final ResourceKey<CreativeModeTab> MAIN_TAB =
            tabKey("main");

    @Test
    void collectsEntriesByTabAndDeclaredSectionOrder() {
        PiCreativeContentRegistry registry = new PiCreativeContentRegistry();
        registry.addStack(MAIN_TAB, "materials", () -> ItemStack.EMPTY);
        registry.addStack(MAIN_TAB, "machines", () -> ItemStack.EMPTY);
        registry.addStack(MAIN_TAB, "unlisted", () -> ItemStack.EMPTY);

        List<PiCreativeEntryPlan> entries = registry.plans(MAIN_TAB, List.of("machines", "materials"));

        assertEquals("machines", entries.get(0).section());
        assertEquals("materials", entries.get(1).section());
        assertEquals("unlisted", entries.get(2).section());
    }

    @Test
    void variantsStayDeferredAndKeepStableVariantIds() {
        PiCreativeContentRegistry registry = new PiCreativeContentRegistry();
        registry.addStack(MAIN_TAB, "scrolls", () -> ItemStack.EMPTY);
        registry.addStackVariant(MAIN_TAB, "scrolls", () -> {
            throw new AssertionError("stack should not resolve while planning");
        }, "fireball", stack -> {
        });

        List<PiCreativeEntryPlan> entries = registry.plans(MAIN_TAB, List.of("scrolls"));

        assertEquals(2, entries.size());
        assertEquals("fireball", entries.get(1).id());
    }

    @Test
    void variantsCanUseTheSameVisibilityAsTheirOwningEntry() {
        PiCreativeContentRegistry registry = new PiCreativeContentRegistry();
        registry.addStackVariant(
                MAIN_TAB,
                "scrolls",
                () -> ItemStack.EMPTY,
                "fireball",
                stack -> {
                },
                PiCreativeVisibility.SEARCH_ONLY);

        List<PiCreativeEntryPlan> entries = registry.plans(MAIN_TAB, List.of("scrolls"));

        assertEquals(1, entries.size());
        assertEquals(PiCreativeVisibility.SEARCH_ONLY, entries.get(0).visibility());
    }

    @Test
    void generatedVariantsExpandAtPlanningTimeAndKeepStacksDeferred() {
        PiCreativeContentRegistry registry = new PiCreativeContentRegistry();
        AtomicInteger sourceCalls = new AtomicInteger();
        AtomicInteger stackCalls = new AtomicInteger();

        registry.addStackVariants(
                MAIN_TAB,
                "scrolls",
                () -> {
                    stackCalls.incrementAndGet();
                    throw new AssertionError("stack should not resolve while planning");
                },
                () -> {
                    sourceCalls.incrementAndGet();
                    return List.of(1, 2, 3);
                },
                level -> "level_" + level,
                (stack, level) -> {
                },
                PiCreativeVisibility.SEARCH_ONLY);

        assertEquals(0, sourceCalls.get());
        assertEquals(0, stackCalls.get());

        List<PiCreativeEntryPlan> entries = registry.plans(MAIN_TAB, List.of("scrolls"));

        assertEquals(1, sourceCalls.get());
        assertEquals(0, stackCalls.get());
        assertEquals(List.of("level_1", "level_2", "level_3"), entries.stream()
                .map(PiCreativeEntryPlan::id)
                .toList());
        assertEquals(PiCreativeVisibility.SEARCH_ONLY, entries.get(0).visibility());
        assertEquals(0, stackCalls.get());
    }

    @Test
    void hiddenEntriesAreNotDisplayedButSearchOnlyEntriesRemainSearchOnly() {
        PiCreativeContentRegistry registry = new PiCreativeContentRegistry();
        registry.addStack(MAIN_TAB, "debug", () -> ItemStack.EMPTY, PiCreativeVisibility.HIDDEN);
        registry.addStack(MAIN_TAB, "debug", () -> ItemStack.EMPTY, PiCreativeVisibility.SEARCH_ONLY);

        List<PiCreativeEntryPlan> entries = registry.plans(MAIN_TAB, List.of("debug"));

        assertEquals(1, entries.size());
        assertEquals(PiCreativeVisibility.SEARCH_ONLY, entries.get(0).visibility());
    }

    @Test
    void storedSectionOrderCanDriveLaterTabPopulation() {
        PiCreativeContentRegistry registry = new PiCreativeContentRegistry();
        registry.order(MAIN_TAB, "machines", "materials");
        registry.addStack(MAIN_TAB, "materials", () -> ItemStack.EMPTY, PiCreativeVisibility.PARENT_ONLY);
        registry.addStack(MAIN_TAB, "machines", () -> ItemStack.EMPTY, PiCreativeVisibility.SEARCH_ONLY);

        List<PiCreativeEntryPlan> entries = registry.plans(MAIN_TAB);

        assertEquals("machines", entries.get(0).section());
        assertEquals("materials", entries.get(1).section());
    }

    @Test
    void emitResolvesVisibleEntriesWithStoredSectionOrder() {
        PiCreativeContentRegistry registry = new PiCreativeContentRegistry();
        registry.order(MAIN_TAB, "machines", "materials");
        registry.addStack(MAIN_TAB, "materials", () -> ItemStack.EMPTY, PiCreativeVisibility.PARENT_ONLY);
        registry.addStack(MAIN_TAB, "machines", () -> ItemStack.EMPTY, PiCreativeVisibility.SEARCH_ONLY);
        List<PiCreativeVisibility> emitted = new ArrayList<>();

        int count = registry.emit(MAIN_TAB, (stack, visibility) -> emitted.add(visibility));

        assertEquals(2, count);
        assertEquals(List.of(PiCreativeVisibility.SEARCH_ONLY, PiCreativeVisibility.PARENT_ONLY), emitted);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ResourceKey<CreativeModeTab> tabKey(String path) {
        try {
            Constructor<ResourceKey> constructor =
                    ResourceKey.class.getDeclaredConstructor(ResourceLocation.class, ResourceLocation.class);
            constructor.setAccessible(true);
            return (ResourceKey<CreativeModeTab>) constructor.newInstance(
                    new ResourceLocation("minecraft", "creative_mode_tab"),
                    id(path));
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Failed to create lightweight creative tab key for unit test", exception);
        }
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("example", path);
    }
}
