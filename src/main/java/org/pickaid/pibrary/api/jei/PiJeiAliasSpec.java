package org.pickaid.pibrary.api.jei;

import java.util.List;
import java.util.Objects;
import net.minecraft.world.item.ItemStack;

/**
 * Alias mapping from one or more item stacks to searchable JEI alias strings.
 *
 * @param itemStacks aliased item stacks
 * @param aliases searchable alias strings
 */
public record PiJeiAliasSpec(
        List<ItemStack> itemStacks,
        List<String> aliases
) {
    public PiJeiAliasSpec {
        Objects.requireNonNull(itemStacks, "itemStacks");
        Objects.requireNonNull(aliases, "aliases");
        itemStacks = itemStacks.stream().map(ItemStack::copy).toList();
        aliases = List.copyOf(aliases);
        if (itemStacks.isEmpty()) {
            throw new IllegalArgumentException("itemStacks must not be empty");
        }
        if (aliases.isEmpty()) {
            throw new IllegalArgumentException("aliases must not be empty");
        }
    }
}
