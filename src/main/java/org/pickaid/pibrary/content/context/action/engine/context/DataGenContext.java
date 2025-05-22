package org.pickaid.pibrary.content.context.action.engine.context;

import org.pickaid.pibrary.content.context.interaction.InteractionAction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public record DataGenContext(BootstapContext<InteractionAction> ctx) {

	public Holder<DamageType> damage(ResourceKey<DamageType> key) {
		return ctx.lookup(Registries.DAMAGE_TYPE).getOrThrow(key);
	}

}
