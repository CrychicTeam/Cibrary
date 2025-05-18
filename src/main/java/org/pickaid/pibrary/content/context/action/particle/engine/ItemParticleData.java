package org.pickaid.pibrary.content.context.action.particle.engine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.particle.render.ItemSprite;
import org.pickaid.pibrary.content.context.action.particle.render.ParticleRenderer;
import org.pickaid.pibrary.content.context.action.particle.render.SpriteGeom;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ItemParticleData(
		RenderTypePreset renderType, Item item, @Nullable SpriteGeom geom
) implements ParticleRenderData<ItemParticleData> {

	public static final Codec<ItemParticleData> CODEC = RecordCodecBuilder.create(i -> i.group(
			RenderTypePreset.CODEC.fieldOf("renderType").forGetter(e -> e.renderType),
			ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(e -> e.item),
			SpriteGeom.CODEC.optionalFieldOf("geom").forGetter(e -> Optional.ofNullable(e.geom))
	).apply(i, (a, b, c) -> new ItemParticleData(a, b, c.orElse(null))));

	@Override
	public ParticleRenderType<ItemParticleData> type() {
		return LibraryRegistries.ITEM_RENDER.get();
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public ParticleRenderer resolve(EngineContext ctx) {
		return new ItemSprite(
				renderType,
				item.getDefaultInstance(),
				geom == null ? SpriteGeom.breaking(ctx.rand()) : geom
		);
	}

}
