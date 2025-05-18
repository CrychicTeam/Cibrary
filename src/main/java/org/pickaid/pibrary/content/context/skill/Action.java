package org.pickaid.pibrary.content.context.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import org.pickaid.pibrary.content.context.action.engine.context.*;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.helper.EngineHelper;
import org.pickaid.pibrary.content.context.action.engine.helper.Scheduler;
import org.pickaid.pibrary.Pibrary;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraftforge.registries.ForgeRegistries;
import org.pickaid.pibrary.network.PibraryNetworkHandler;

import java.util.Set;

public record Action(ConfiguredEngine<?> action, Item icon, int order,
					 ActionCastType castType, ActionTriggerType triggerType) {

	private static final Codec<ActionCastType> CAST_CODEC = EngineHelper.enumCodec(ActionCastType.class, ActionCastType.values());
	private static final Codec<ActionTriggerType> TRIGGER_CODEC = EngineHelper.enumCodec(ActionTriggerType.class, ActionTriggerType.values());

	public static final Codec<Action> CODEC = RecordCodecBuilder.create(i -> i.group(
			ConfiguredEngine.codec("action", Action::action),
			ForgeRegistries.ITEMS.getCodec().fieldOf("icon").forGetter(e -> e.icon),
			Codec.INT.fieldOf("order").forGetter(e -> e.order),
			CAST_CODEC.fieldOf("cast_type").forGetter(e -> e.castType),
			TRIGGER_CODEC.fieldOf("trigger_type").forGetter(e -> e.triggerType)
	).apply(i, Action::new));

	public static String lang(ResourceLocation rl) {
		return "spell_action." + rl.getNamespace() + "." + rl.getPath();
	}

	public Set<String> params() {
		return ActionContext.DEFAULT_PARAMS;
	}

	public void execute(ActionContext ctx) {
		var sche = new Scheduler();
		try {
			var source = new SingleThreadedRandomSource(ctx.seed());
			EngineContext engine = new EngineContext(
					new UserContext(ctx.user().level(), ctx.user(), sche),
					LocationContext.of(ctx.origin(), ctx.facing()),
					source, ctx.defaultArgs()
			);
			action().execute(engine);
			engine.registerScheduler();
		} catch (Exception e) {
			Pibrary.LOGGER.throwing(e);
			return;
		}
		if (!ctx.user().level().isClientSide()) {
			if (ctx.user() instanceof ServerPlayer player) {
				PibraryNetworkHandler.action(player, new ActionPacket(this, ctx));
			}
		}
	}

	public boolean verify(ResourceLocation id) {
		return action().verify(BuilderContext.withScheduler(Pibrary.LOGGER, id.toString(), params()));
	}

	public void verifyOnBuild(BootstapContext<Action> ctx, ResourceKey<Action> id) {
		verify(id.location());
		ctx.register(id, this);
	}

}
