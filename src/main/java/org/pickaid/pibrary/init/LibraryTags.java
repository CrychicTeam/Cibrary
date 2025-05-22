package org.pickaid.pibrary.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.pickaid.pibrary.Pibrary;

public class LibraryTags {
    public static final TagKey<Item> FAST_USE = TagKey.create(Registries.ITEM, Pibrary.source("fast_use"));
}
