package org.pickaid.pibrary.api.creative;

import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/**
 * Resolved item stack ready to be inserted into a creative tab.
 *
 * @param section section that produced this entry
 * @param stack stack to show
 * @param visibility creative search/display visibility
 */
public record PiCreativeEntry(
        String section,
        ItemStack stack,
        PiCreativeVisibility visibility
) {
    public PiCreativeEntry {
        Objects.requireNonNull(section, "section");
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(visibility, "visibility");
    }
}
