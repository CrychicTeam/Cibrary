package org.pickaid.pibrary.content.context.action.conditions.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;
import org.pickaid.pibrary.content.context.action.conditions.predicates.params.StatePropertiesPredicate;

import java.util.List;

public record FluidPredicate(HolderSet<Fluid> fluids, StatePropertiesPredicate properties) {
	private static final Codec<Holder<Fluid>> FLUID_CODEC = ForgeRegistries.FLUIDS.getCodec().xmap(ForgeRegistries.FLUIDS::getDelegateOrThrow, Holder::get);
	private static final Codec<HolderSet<Fluid>> FLUIDS_CODEC = FLUID_CODEC.listOf().xmap(HolderSet::direct, holders -> holders.stream().toList());
	public static final FluidPredicate ANY = new FluidPredicate(HolderSet.direct(List.of()), StatePropertiesPredicate.ANY);
	public static final Codec<FluidPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			FLUIDS_CODEC.optionalFieldOf("fluids", HolderSet.direct(List.of())).forGetter(FluidPredicate::fluids),
			StatePropertiesPredicate.CODEC.optionalFieldOf("state", StatePropertiesPredicate.ANY).forGetter(FluidPredicate::properties)
	).apply(instance, FluidPredicate::new));
	
	public boolean matches(ServerLevel level, BlockPos pos) {
		if (this == ANY) return true;
		FluidState fluidState = level.getFluidState(pos);
		if (!this.fluids.contains(fluidState.holder())) {
			return false;
		}
		if (properties().matches(fluidState)) {
			return false;
		}
		return true;
	}
}
