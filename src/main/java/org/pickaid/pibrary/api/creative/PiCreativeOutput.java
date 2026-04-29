package org.pickaid.pibrary.api.creative;

import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

/**
 * Receives deferred creative-tab stacks from {@link PiCreativeContentRegistry}.
 */
@FunctionalInterface
public interface PiCreativeOutput {
    void accept(Supplier<ItemStack> stack, PiCreativeVisibility visibility);
}
