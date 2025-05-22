package org.pickaid.pibrary.content.context.conditions.predicates.params;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.List;

public record StructurePredicate(HolderSet<Structure> structures) {
	public static final StructurePredicate ANY = new StructurePredicate(HolderSet.direct(List.of()));
	public static final Codec<StructurePredicate> CODEC = RegistryCodecs.homogeneousList(Registries.STRUCTURE, Structure.DIRECT_CODEC).xmap(StructurePredicate::new, StructurePredicate::structures);
	
	public boolean matches(ServerLevel level, BlockPos pos) {
		if (this == ANY) return true;
		for (Holder<Structure> structure : structures) {
			if (level.structureManager().getStructureAt(pos, structure.get()).isValid()) {
				return true;
			}
		}
		return false;
	}
}
