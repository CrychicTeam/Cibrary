package org.crychicteam.cibrary.content.fastprojectileapi.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.crychicteam.cibrary.content.fastprojectileapi.entity.SimplifiedProjectile;

public interface RenderableProjectileType<T extends RenderableProjectileType<T, I>, I> {

	void start(MultiBufferSource buffer, Iterable<I> list);

	void create(ProjectileRenderer r, SimplifiedProjectile e, PoseStack pose, float pTick);

}
