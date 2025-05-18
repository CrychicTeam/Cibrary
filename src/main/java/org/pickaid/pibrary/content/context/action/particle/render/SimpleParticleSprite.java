package org.pickaid.pibrary.content.context.action.particle.render;

import org.pickaid.pibrary.content.context.action.particle.engine.RenderTypePreset;
import net.minecraft.resources.ResourceLocation;

public record SimpleParticleSprite(RenderTypePreset renderType, ResourceLocation particle) implements VanillaParticleSprite {

}
