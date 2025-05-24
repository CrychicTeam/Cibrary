package org.pickaid.pibrary.content.context.interaction;

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
import org.pickaid.pibrary.network.action.ActionPacket;
import org.pickaid.pibrary.network.PibraryNetworkHandler;

import java.util.Set;

public record InteractionAction(ConfiguredEngine<?> action, Item icon, int order,
								InteractionActionCastType castType, InteractionActionTriggerType triggerType) {

	private static final Codec<InteractionActionCastType> CAST_CODEC = EngineHelper.enumCodec(InteractionActionCastType.class, InteractionActionCastType.values());
	private static final Codec<InteractionActionTriggerType> TRIGGER_CODEC = EngineHelper.enumCodec(InteractionActionTriggerType.class, InteractionActionTriggerType.values());

	public static final Codec<InteractionAction> CODEC = RecordCodecBuilder.create(i -> i.group(
			ConfiguredEngine.codec("action", InteractionAction::action),
			ForgeRegistries.ITEMS.getCodec().fieldOf("icon").forGetter(e -> e.icon),
			Codec.INT.fieldOf("order").forGetter(e -> e.order),
			CAST_CODEC.fieldOf("cast_type").forGetter(e -> e.castType),
			TRIGGER_CODEC.fieldOf("trigger_type").forGetter(e -> e.triggerType)
	).apply(i, InteractionAction::new));

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
		Set<String> allParams = collectAllRequiredParams();
		return action().verify(BuilderContext.withScheduler(Pibrary.LOGGER, id.toString(), allParams));
	}

	private Set<String> collectAllRequiredParams() {
		Set<String> baseParams = params();
		Set<String> actionParams = action().verificationParameters();

		if (actionParams != null && !actionParams.isEmpty()) {
			Set<String> allParams = new java.util.HashSet<>(baseParams);
			allParams.addAll(actionParams);
			return allParams;
		}

		return baseParams;
	}

	public void verifyOnBuild(BootstapContext<InteractionAction> ctx, ResourceKey<InteractionAction> id) {
		verify(id.location());
		ctx.register(id, this);
	}

}