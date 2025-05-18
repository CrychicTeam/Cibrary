package org.pickaid.pibrary.content.context.action.particle.engine;

import com.mojang.serialization.Codec;
import org.pickaid.pibrary.content.context.action.engine.helper.EngineHelper;

public enum RenderTypePreset {
	NORMAL, LIT, TRANSLUCENT, BLOCK, BLOCK_LIT;

	public static final Codec<RenderTypePreset> CODEC =
			EngineHelper.enumCodec(RenderTypePreset.class, RenderTypePreset.values());
}
