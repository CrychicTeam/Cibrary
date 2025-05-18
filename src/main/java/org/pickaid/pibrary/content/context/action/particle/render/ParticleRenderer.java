package org.pickaid.pibrary.content.context.action.particle.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.pickaid.pibrary.content.context.action.particle.core.LMGenericParticle;
import org.pickaid.pibrary.content.context.action.particle.engine.RenderTypePreset;
import net.minecraft.client.Camera;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ParticleRenderer {

	@OnlyIn(Dist.CLIENT)
	void onParticleInit(LMGenericParticle e);

	@OnlyIn(Dist.CLIENT)
	void onPostTick(LMGenericParticle e);

	RenderTypePreset renderType();

	@OnlyIn(Dist.CLIENT)
	boolean specialRender(LMGenericParticle lmGenericParticle, VertexConsumer vc, Camera camera, float pTick);

	default boolean needOrientation(){
		return false;
	}

}
