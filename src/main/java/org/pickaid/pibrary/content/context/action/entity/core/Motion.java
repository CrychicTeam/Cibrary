package org.pickaid.pibrary.content.context.action.entity.core;

import com.mojang.serialization.Codec;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.Verifiable;
import org.pickaid.pibrary.content.fastprojectileapi.entity.ProjectileMovement;
import org.pickaid.pibrary.init.LibraryRegistries;

public interface Motion<T extends Record & Motion<T>> extends Verifiable {

	Codec<Motion<?>> CODEC = LibraryRegistries.MOTION.codec()
			.dispatch(Motion::type, MotionType::codec);

	MotionType<T> type();

	ProjectileMovement move(EngineContext ctx, Vec3 vec, Vec3 pos);

}
