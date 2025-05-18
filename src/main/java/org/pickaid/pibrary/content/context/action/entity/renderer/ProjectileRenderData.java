package org.pickaid.pibrary.content.context.action.entity.renderer;

import com.mojang.serialization.Codec;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ProjectileRenderData<T extends Record & ProjectileRenderData<T>> {

	Codec<ProjectileRenderData<?>> CODEC = LibraryRegistries.PROJECTILE_RENDERER.codec()
			.dispatch(ProjectileRenderData::type, ProjectileRenderType::codec);

	ProjectileRenderType<T> type();

	@OnlyIn(Dist.CLIENT)
	ProjectileRenderer resolve(EngineContext ctx);

}
