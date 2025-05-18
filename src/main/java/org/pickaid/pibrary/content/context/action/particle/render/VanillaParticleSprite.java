package org.pickaid.pibrary.content.context.action.particle.render;

import dev.xkmc.Pibrary.mixin.ParticleEngineAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface VanillaParticleSprite extends SpriteData {

	ResourceLocation particle();

	@OnlyIn(Dist.CLIENT)
	@Override
	default SpriteSet spriteSet() {
		return ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).getSpriteSets().get(particle());
	}

}
