package org.pickaid.pibrary.api.registrate;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

final class PiCreativeTabTarget {
    private PiCreativeTabTarget() {
    }

    static Supplier<ResourceKey<CreativeModeTab>> explicit(ResourceKey<CreativeModeTab> tab) {
        ResourceKey<CreativeModeTab> target = Objects.requireNonNull(tab, "tab");
        return () -> target;
    }

    static Supplier<ResourceKey<CreativeModeTab>> defaultTab(Supplier<ResourceKey<CreativeModeTab>> defaultTab) {
        Objects.requireNonNull(defaultTab, "defaultTab");
        ResourceKey<CreativeModeTab> current = defaultTab.get();
        if (current != null) {
            return () -> current;
        }
        return () -> {
            ResourceKey<CreativeModeTab> resolved = defaultTab.get();
            if (resolved == null) {
                throw new IllegalStateException("default creative tab must be set before creative contents are registered");
            }
            return resolved;
        };
    }
}
