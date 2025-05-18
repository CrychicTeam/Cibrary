package org.pickaid.pibrary.content.context.action.conditions.predicates;

import org.pickaid.pibrary.content.utils.Utils;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;

public record DimensionPredicate(List<ResourceKey<Level>> dimensions) {
	private static final Codec<ResourceKey<Level>> DIMENSION_CODEC = Utils.resourceKeyCodec(Registries.DIMENSION);
	private static final Codec<List<ResourceKey<Level>>> DIMENSIONS_CODEC = Utils.resourceKeyCodec(Registries.DIMENSION).listOf();
	public static final DimensionPredicate ANY = new DimensionPredicate(List.of());
	public static final Codec<DimensionPredicate> CODEC = Codec.either(DIMENSION_CODEC, DIMENSIONS_CODEC).xmap(either -> either.map(key -> new DimensionPredicate(List.of(key)), DimensionPredicate::new), dimension -> dimension.dimensions.size() == 1 ? Either.left(dimension.dimensions.get(0)) : Either.right(dimension.dimensions));
	
	public boolean matches(ServerLevel level) {
		if (this == ANY) return true;
		return this.dimensions.contains(level.dimension());
	}
}
