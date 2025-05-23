package org.pickaid.pibrary.content.context.action.entity.renderer;

import com.mojang.serialization.Codec;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.init.LibraryRegistries;

public interface ProjectileRenderData<T extends Record & ProjectileRenderData<T>> {

	Codec<ProjectileRenderData<?>> CODEC = ExtraCodecs.lazyInitializedCodec(() -> LibraryRegistries.PROJECTILE_RENDERER_TYPE.get().getCodec()
			.dispatch(ProjectileRenderData::type, ProjectileRenderType::codec));

	ProjectileRenderType<T> type();

	@OnlyIn(Dist.CLIENT)
	ProjectileRenderer resolve(EngineContext ctx);

}
