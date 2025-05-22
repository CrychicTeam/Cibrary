package org.pickaid.pibrary.content.context.action.particle.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.pickaid.pibrary.content.context.action.particle.core.PiGenericParticle;
import org.pickaid.pibrary.content.context.action.particle.engine.RenderTypePreset;
import net.minecraft.client.Camera;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ParticleRenderer {

	@OnlyIn(Dist.CLIENT)
	void onParticleInit(PiGenericParticle e);

	@OnlyIn(Dist.CLIENT)
	void onPostTick(PiGenericParticle e);

	RenderTypePreset renderType();

	@OnlyIn(Dist.CLIENT)
	boolean specialRender(PiGenericParticle lmGenericParticle, VertexConsumer vc, Camera camera, float pTick);

	default boolean needOrientation(){
		return false;
	}

}
