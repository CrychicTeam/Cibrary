package org.pickaid.pibrary.content.context.action.engine.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.EngineType;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.content.context.variable.IntVariable;
import org.pickaid.pibrary.content.context.action.entity.motion.SimpleMotion;
import org.pickaid.pibrary.content.context.action.particle.core.ClientParticleData;
import org.pickaid.pibrary.content.context.action.particle.core.PiGenericParticleOption;
import org.pickaid.pibrary.content.context.action.particle.engine.RenderTypePreset;
import org.pickaid.pibrary.content.context.action.particle.render.ItemSprite;
import org.pickaid.pibrary.content.context.action.particle.render.SpriteGeom;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public record ItemParticleInstance(
		Item item, DoubleVariable speed,
		DoubleVariable scale, IntVariable life, boolean breaking
) implements ParticleInstance<ItemParticleInstance> {

	public static final Codec<ItemParticleInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
			ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(e -> e.item),
			DoubleVariable.codec("speed", ParticleInstance::speed),
			DoubleVariable.codec("scale", e -> e.scale),
			IntVariable.codec("life", e -> e.life),
			Codec.BOOL.fieldOf("breaking").forGetter(e -> e.breaking)
	).apply(i, ItemParticleInstance::new));

	@Override
	public EngineType<ItemParticleInstance> type() {
		return LibraryRegistries.ITEM_PARTICLE.get();
	}

	@Override
	public ParticleOptions particle(EngineContext ctx) {
		return new PiGenericParticleOption(new ClientParticleData(
				life.eval(ctx), breaking, (float) scale.eval(ctx) * ClientParticleData.randSize(ctx), ctx,
				breaking() ? SimpleMotion.BREAKING : SimpleMotion.ZERO,
				new ItemSprite(RenderTypePreset.BLOCK, item.getDefaultInstance(),
						breaking ? SpriteGeom.breaking(ctx.rand()) : SpriteGeom.INSTANCE)
		));
	}

}
