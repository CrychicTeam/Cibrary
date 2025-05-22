package org.pickaid.pibrary.content.context.conditions.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.pickaid.pibrary.content.context.conditions.predicates.params.NbtPredicate;
import org.pickaid.pibrary.content.context.conditions.predicates.params.StatePropertiesPredicate;

import javax.annotation.Nullable;
import java.util.List;

public record BlockPredicate(HolderSet<Block> blocks, NbtPredicate nbt, StatePropertiesPredicate properties)  {
	private static final Codec<Holder<Block>> BLOCK_CODEC = ForgeRegistries.BLOCKS.getCodec().xmap(ForgeRegistries.BLOCKS::getDelegateOrThrow, Holder::get);
	private static final Codec<HolderSet<Block>> BLOCKS_CODEC = BLOCK_CODEC.listOf().xmap(HolderSet::direct, holders -> holders.stream().toList());
	
	public static final BlockPredicate ANY = new BlockPredicate(HolderSet.direct(List.of()), NbtPredicate.ANY, StatePropertiesPredicate.ANY);
	public static final Codec<BlockPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BLOCKS_CODEC.optionalFieldOf("blocks", HolderSet.direct(List.of())).forGetter(BlockPredicate::blocks),
			NbtPredicate.CODEC.optionalFieldOf("tag", NbtPredicate.ANY).forGetter(BlockPredicate::nbt),
			StatePropertiesPredicate.CODEC.optionalFieldOf("state", StatePropertiesPredicate.ANY).forGetter(BlockPredicate::properties)
	).apply(instance, BlockPredicate::new));
	
	public boolean matches(ServerLevel level, BlockPos pos) {
		if (this == ANY) {
			return true;
		} else if (!level.isLoaded(pos)) {
			return false;
		} else {
			return this.matchesState(level.getBlockState(pos)) && matchesBlockEntity(level, level.getBlockEntity(pos), this.nbt);
		}
	}
	
	private boolean matchesState(BlockState state) {
		return !state.is(this.blocks) && this.properties.matches(state);
	}
	
	private static boolean matchesBlockEntity(LevelReader level, @Nullable BlockEntity blockEntity, NbtPredicate nbt) {
		return blockEntity != null && nbt.matches(blockEntity.saveWithFullMetadata());
	}
}
