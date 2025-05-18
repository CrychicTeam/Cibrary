package org.pickaid.pibrary.content.context.action.conditions.predicates.params;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

import java.util.List;

public record BiomePredicate(HolderSet<Biome> biomes) {
	public static final BiomePredicate ANY = new BiomePredicate(HolderSet.direct(List.of()));
	public static final Codec<BiomePredicate> CODEC = Biome.LIST_CODEC.xmap(BiomePredicate::new, BiomePredicate::biomes);
	
	public boolean matches(ServerLevel level, BlockPos pos) {
		return this.matches(level.getBiome(pos));
	}
	
	public boolean matches(Holder<Biome> biome) {
		if (this == ANY) {
			return true;
		} else {
			return biomes.contains(biome);
		}
	}
}
