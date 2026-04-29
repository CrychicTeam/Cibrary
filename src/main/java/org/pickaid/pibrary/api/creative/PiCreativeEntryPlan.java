package org.pickaid.pibrary.api.creative;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

/**
 * Deferred creative entry before its {@link ItemStack} is resolved.
 *
 * @param section section that owns the entry
 * @param id entry id inside the section
 * @param stack stack factory
 * @param visibility creative search/display visibility
 */
public record PiCreativeEntryPlan(
        String section,
        String id,
        Supplier<ItemStack> stack,
        PiCreativeVisibility visibility
) {
    public PiCreativeEntryPlan {
        Objects.requireNonNull(section, "section");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(visibility, "visibility");
    }
}
