package org.pickaid.pibrary.runtime.registry;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.Registrate;
import java.lang.reflect.Field;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.registry.PiRegistries;
import org.pickaid.pibrary.api.registry.PiRegistryFamily;
import org.pickaid.pibrary.api.registry.PiRegistryPlan;
import org.pickaid.pibrary.testsupport.PiTestRegistryKeys;

class PiRegistratePlansTest {
    private static final ResourceKey<Registry<String>> STRINGS = PiTestRegistryKeys.registry("strings");

    @Test
    void simpleAdapterRejectsPlansFromOtherNamespacesBeforeRegistering() {
        PiRegistryFamily<String> strings = PiRegistries.family(STRINGS, "other_mod");
        PiRegistryPlan plan = PiRegistries.collect(sink -> sink.register(strings.entry("wand", () -> "wand")));

        assertThrows(IllegalStateException.class, () -> PiRegistratePlans.simple(registrate("example"), plan));
    }

    private static Registrate registrate(String modid) {
        try {
            Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
            Registrate registrate = (Registrate) unsafe.allocateInstance(Registrate.class);
            Field modidField = AbstractRegistrate.class.getDeclaredField("modid");
            modidField.setAccessible(true);
            modidField.set(registrate, modid);
            return registrate;
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Failed to create Registrate for unit test", exception);
        }
    }
}
