package org.pickaid.pibrary.content.context.action.entity.core;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.Verifiable;
import org.pickaid.pibrary.api.fastprojectileapi.entity.ProjectileMovement;
import org.pickaid.pibrary.init.LibraryObjects;
import org.pickaid.pibrary.init.LibraryRegistries;

public interface Motion<T extends Record & Motion<T>> extends Verifiable {

	Codec<Motion<?>> CODEC = ExtraCodecs.lazyInitializedCodec(() -> LibraryRegistries.MOTION_TYPE.get().getCodec()
			.dispatch(Motion::type, MotionType::codec));

	MotionType<T> type();

	ProjectileMovement move(EngineContext ctx, Vec3 vec, Vec3 pos);

}
