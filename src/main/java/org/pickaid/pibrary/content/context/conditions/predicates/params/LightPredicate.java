package org.pickaid.pibrary.content.context.conditions.predicates.params;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.pickaid.pibrary.tools.utils.CodecUtils;

public record LightPredicate(MinMaxBounds.Ints composite) {
	public static final Codec<LightPredicate> CODEC = CodecUtils.BOUNDS_INTS.xmap(LightPredicate::new, LightPredicate::composite);
	public static final LightPredicate ANY = new LightPredicate(MinMaxBounds.Ints.ANY);
	
	
	public boolean matches(ServerLevel pLevel, BlockPos pPos) {
		if (this == ANY) {
			return true;
		} else if (!pLevel.isLoaded(pPos)) {
			return false;
		} else {
			return this.composite.matches(pLevel.getMaxLocalRawBrightness(pPos));
		}
	}
}
