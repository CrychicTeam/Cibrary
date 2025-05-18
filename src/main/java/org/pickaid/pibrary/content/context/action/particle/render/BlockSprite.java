package org.pickaid.pibrary.content.context.action.particle.render;

import org.pickaid.pibrary.content.context.action.particle.core.LMGenericParticle;
import org.pickaid.pibrary.content.context.action.particle.engine.RenderTypePreset;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public record BlockSprite(
		RenderTypePreset renderType, BlockState state, BlockPos pos, SpriteGeom geom
) implements ModelSpriteData {

	@Override
	public void onParticleInit(LMGenericParticle e) {
		var shaper = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper();
		e.setSprite(shaper.getTexture(state, e.level(), pos));
		int i = Minecraft.getInstance().getBlockColors().getColor(state, e.level(), pos, 0);
		e.setColor(
				(i >> 16 & 255) / 255.0F,
				(i >> 8 & 255) / 255.0F,
				(i & 255) / 255.0F
		);
		e.setGeom(geom);
	}

}
