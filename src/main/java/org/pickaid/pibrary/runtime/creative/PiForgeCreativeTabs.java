package org.pickaid.pibrary.runtime.creative;

import java.util.Objects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.pickaid.pibrary.api.creative.PiCreativeContentRegistry;
import org.pickaid.pibrary.api.creative.PiCreativeVisibility;

/**
 * Forge bridge for Pibrary creative-tab entries.
 */
public final class PiForgeCreativeTabs {
    private PiForgeCreativeTabs() {
    }

    public static void registerTo(IEventBus modBus, PiCreativeContentRegistry registry) {
        Objects.requireNonNull(modBus, "modBus");
        Objects.requireNonNull(registry, "registry");
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> populate(event, registry));
    }

    public static int populate(BuildCreativeModeTabContentsEvent event, PiCreativeContentRegistry registry) {
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(registry, "registry");
        return registry.emit(event.getTabKey(), (stack, visibility) ->
                event.accept(stack.get(), toForgeVisibility(visibility)));
    }

    static CreativeModeTab.TabVisibility toForgeVisibility(PiCreativeVisibility visibility) {
        return switch (Objects.requireNonNull(visibility, "visibility")) {
            case PARENT_AND_SEARCH -> CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;
            case PARENT_ONLY -> CreativeModeTab.TabVisibility.PARENT_TAB_ONLY;
            case SEARCH_ONLY -> CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY;
            case HIDDEN -> throw new IllegalArgumentException("hidden creative entries must be filtered before output");
        };
    }
}
