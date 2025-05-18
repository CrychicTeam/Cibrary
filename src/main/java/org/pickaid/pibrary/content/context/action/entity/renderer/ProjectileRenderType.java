package org.pickaid.pibrary.content.context.action.entity.renderer;

import com.mojang.serialization.Codec;

public interface ProjectileRenderType<T extends Record & ProjectileRenderData<T>> {

	Codec<T> codec();

}
