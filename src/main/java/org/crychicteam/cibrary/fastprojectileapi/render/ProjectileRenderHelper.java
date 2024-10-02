package org.crychicteam.cibrary.fastprojectileapi.render;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import dev.xkmc.l2serial.util.Wrappers;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.crychicteam.cibrary.Cibrary;

import java.util.Map;
import java.util.Set;


@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Cibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ProjectileRenderHelper {

	private static final Map<RenderableProjectileType<?, ?>, Set<?>> MAP = Maps.newConcurrentMap();

	public static <T extends RenderableProjectileType<T, I>, I> Set<I> setOf(RenderableProjectileType<T, I> key) {
		return Wrappers.cast(MAP.computeIfAbsent(key, l -> Sets.newConcurrentHashSet()));
	}

	public static <T extends RenderableProjectileType<T, I>, I> void add(RenderableProjectileType<T, I> key, I ins) {
		setOf(key).add(ins);
	}

	@SubscribeEvent
	public static void renderLate(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
		var buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		for (var ent : MAP.entrySet()) {
			ent.getKey().start(buffer, Wrappers.cast(ent.getValue()));
		}
		buffer.endLastBatch();
		MAP.clear();

	}
}
