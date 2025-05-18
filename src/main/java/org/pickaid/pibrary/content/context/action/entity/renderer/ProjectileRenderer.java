package org.pickaid.pibrary.content.context.action.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.pickaid.pibrary.content.context.action.entity.core.LMProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public interface ProjectileRenderer {

	ResourceLocation getTexture();

	void render(LMProjectile e, float pTick, PoseStack pose, MultiBufferSource buffer, int light);

}
