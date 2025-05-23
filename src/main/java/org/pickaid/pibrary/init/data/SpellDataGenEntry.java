package org.pickaid.pibrary.init.data;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.context.interaction.InteractionAction;
import org.pickaid.pibrary.init.LibraryRegistries;

public abstract class SpellDataGenEntry {

	protected static ResourceKey<InteractionAction> spell(String id) {
		return ResourceKey.create(LibraryRegistries.ACTION, Pibrary.source(id));
	}

	public abstract void register(BootstapContext<InteractionAction> ctx);

}
