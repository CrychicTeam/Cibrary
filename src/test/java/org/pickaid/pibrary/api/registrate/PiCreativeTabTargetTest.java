package org.pickaid.pibrary.api.registrate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.junit.jupiter.api.Test;

class PiCreativeTabTargetTest {
    @Test
    void capturesExistingDefaultTabAtSectionDeclarationTime() {
        AtomicReference<ResourceKey<CreativeModeTab>> current = new AtomicReference<>(tabKey("main"));

        Supplier<ResourceKey<CreativeModeTab>> target = PiCreativeTabTarget.defaultTab(current::get);
        current.set(tabKey("debug"));

        assertEquals(tabKey("main"), target.get());
    }

    @Test
    void defersMissingDefaultTabUntilCreativeContentIsRegistered() {
        AtomicReference<ResourceKey<CreativeModeTab>> current = new AtomicReference<>();

        Supplier<ResourceKey<CreativeModeTab>> target = PiCreativeTabTarget.defaultTab(current::get);
        current.set(tabKey("main"));

        assertEquals(tabKey("main"), target.get());
    }

    @Test
    void reportsClearErrorIfNoDefaultTabEverExists() {
        Supplier<ResourceKey<CreativeModeTab>> target = PiCreativeTabTarget.defaultTab(() -> null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, target::get);

        assertEquals("default creative tab must be set before creative contents are registered", exception.getMessage());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static ResourceKey<CreativeModeTab> tabKey(String path) {
        try {
            Constructor<ResourceKey> constructor =
                    ResourceKey.class.getDeclaredConstructor(ResourceLocation.class, ResourceLocation.class);
            constructor.setAccessible(true);
            return (ResourceKey<CreativeModeTab>) constructor.newInstance(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "creative_mode_tab"),
                    ResourceLocation.fromNamespaceAndPath("example", path));
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Failed to create lightweight creative tab key for unit test", exception);
        }
    }
}
