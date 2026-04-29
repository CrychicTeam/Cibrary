package org.pickaid.pibrary.testsupport;

import java.lang.reflect.Constructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegisterEvent;

public final class PiTestRegisterEvents {
    private PiTestRegisterEvents() {
    }

    public static RegisterEvent registerEvent(ResourceKey<? extends Registry<?>> registryKey) {
        try {
            Class<?> forgeRegistry = Class.forName("net.minecraftforge.registries.ForgeRegistry");
            Constructor<RegisterEvent> constructor =
                    RegisterEvent.class.getDeclaredConstructor(ResourceKey.class, forgeRegistry, Registry.class);
            constructor.setAccessible(true);
            return constructor.newInstance(registryKey, null, null);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Failed to create RegisterEvent for unit test", exception);
        }
    }
}
