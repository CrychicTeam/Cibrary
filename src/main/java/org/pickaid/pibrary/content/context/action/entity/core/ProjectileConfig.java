package org.pickaid.pibrary.content.context.action.entity.core;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.core.EntityProcessor;
import org.pickaid.pibrary.content.context.action.engine.selector.SelectionType;
import org.pickaid.pibrary.content.context.action.entity.renderer.ProjectileRenderData;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.init.LibraryRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public record ProjectileConfig(
		Set<String> params,
		SelectionType filter,
		@Nullable Motion<?> motion,
		@Nullable ConfiguredEngine<?> tick,
		@Nullable EntityProcessor<?> hit,
		@Nullable ProjectileRenderData<?> renderer
) {

	public static final Codec<ProjectileConfig> CODEC = RecordCodecBuilder.create(i -> {
		return i.group(
				Codec.list(Codec.STRING).optionalFieldOf("params").forGetter(e -> Optional.of(new ArrayList<>(e.params))),
				SelectionType.CODEC.optionalFieldOf("filter").forGetter(e -> Optional.of(e.filter)),
				Motion.CODEC.optionalFieldOf("motion").forGetter(e -> Optional.ofNullable(e.motion)),
				ConfiguredEngine.optionalCodec("tick", e -> e.tick),
				EntityProcessor.CODEC.optionalFieldOf("hit").forGetter(e -> Optional.ofNullable(e.hit)),
				ProjectileRenderData.CODEC.optionalFieldOf("renderer").forGetter(e -> Optional.ofNullable(e.renderer))
		).apply(i, (params, filter, motion, tick, hit, render) -> new ProjectileConfig(
				params.map(LinkedHashSet::new).orElse(new LinkedHashSet<>()),
				filter.orElse(SelectionType.NONE),
				motion.orElse(null),
				tick.orElse(null),
				hit.orElse(null),
				render.orElse(null)
		));
	});

	public static final Codec<Holder<ProjectileConfig>> HOLDER =
			RegistryFileCodec.create(LibraryRegistries.PROJECTILE, CODEC, false);

	public void verify(ResourceLocation id) {
		var allParams = Sets.union(ProjectileData.DEFAULT_PARAMS, params);
		var withSche = BuilderContext.withScheduler(Pibrary.LOGGER, id.toString(), allParams);
		var noSche = BuilderContext.instant(Pibrary.LOGGER, id.toString(), allParams);
		if (motion != null) motion.verify(noSche.of("motion"));
		if (tick != null) tick.verify(withSche.of("tick"));
		if (hit != null) hit.verify(noSche.of("hit"));
	}
}