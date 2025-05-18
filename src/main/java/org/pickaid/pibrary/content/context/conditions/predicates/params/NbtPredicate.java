package org.pickaid.pibrary.content.context.action.conditions.predicates.params;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import org.jetbrains.annotations.Nullable;

public record NbtPredicate(@Nullable CompoundTag tag) {
	public static final NbtPredicate ANY = new NbtPredicate(null);
	public static final Codec<NbtPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CompoundTag.CODEC.fieldOf("tag").orElse(null).forGetter(NbtPredicate::tag)
	).apply(instance, NbtPredicate::new));
	
	
	public boolean matches(CompoundTag tag) {
		return this == ANY || this.tag == null || NbtUtils.compareNbt(this.tag, tag, true);
	}
}
