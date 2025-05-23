package org.pickaid.pibrary.init.data.spell.ice;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import org.pickaid.pibrary.content.context.action.engine.context.*;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.iterator.*;
import org.pickaid.pibrary.content.context.action.engine.logic.*;
import org.pickaid.pibrary.content.context.action.engine.modifier.*;
import org.pickaid.pibrary.content.context.action.engine.particle.SimpleParticleInstance;
import org.pickaid.pibrary.content.context.action.engine.processor.*;
import org.pickaid.pibrary.content.context.action.engine.selector.*;
import org.pickaid.pibrary.content.context.action.entity.motion.MovePosMotion;
import org.pickaid.pibrary.content.context.action.particle.engine.*;
import org.pickaid.pibrary.content.context.interaction.InteractionAction;
import org.pickaid.pibrary.content.context.interaction.InteractionActionCastType;
import org.pickaid.pibrary.content.context.interaction.InteractionActionTriggerType;
import org.pickaid.pibrary.content.context.variable.*;
import org.pickaid.pibrary.init.data.SpellDataGenEntry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.Items;

import java.util.List;

public class WinterStorm extends SpellDataGenEntry {

	public static final ResourceKey<InteractionAction> WINTER = spell("winter_storm");
	public static final ResourceKey<InteractionAction> TORNADO = spell("tornado");

	@Override
	public void register(BootstapContext<InteractionAction> ctx) {
		new InteractionAction(
				winterStorm(new DataGenContext(ctx), 4, 1.5, 1),
				Items.SNOWBALL, 100,
				InteractionActionCastType.CONTINUOUS,
				InteractionActionTriggerType.SELF_POS
		).verifyOnBuild(ctx, WINTER);
		new InteractionAction(
				tornado(new DataGenContext(ctx)),
				Items.POWDER_SNOW_BUCKET, 100,
				InteractionActionCastType.CONTINUOUS,
				InteractionActionTriggerType.FACING_FRONT
		).verifyOnBuild(ctx, TORNADO);
	}

	private static ConfiguredEngine<?> winterStorm(DataGenContext ctx, double r, double y, double size) {
		return new ListLogic(List.of(
				new PredicateLogic(
						BooleanVariable.of("TickUsing>=10"),
						new ProcessorEngine(SelectionType.ENEMY,
								new ArcCubeSelector(
										IntVariable.of("11"),
										DoubleVariable.of(r + ""),
										DoubleVariable.of(size * 2 + ""),
										DoubleVariable.of("-180"),
										DoubleVariable.of("-180+360/12*11")
								),
								List.of(
										new DamageProcessor(ctx.damage(DamageTypes.FREEZE),
												DoubleVariable.of("4"), true, true),
										new PushProcessor(
												DoubleVariable.of("0.1"),
												DoubleVariable.of("75"),
												DoubleVariable.ZERO,
												PushProcessor.Type.TO_CENTER
										)
								)), null),
				new DelayedIterator(
						IntVariable.of("10"),
						IntVariable.of("2"),
						new RingRandomIterator(
								DoubleVariable.of((r - size) + ""),
								DoubleVariable.of((r + size) + ""),
								DoubleVariable.of("-180"),
								DoubleVariable.of("180"),
								IntVariable.of("5"),
								new MoveEngine(List.of(
										RotationModifier.of("75"),
										OffsetModifier.of("0", "rand(" + (y - size) + "," + (y + size) + ")", "0")),
										new SimpleParticleInstance(
												ParticleTypes.SNOWFLAKE,
												DoubleVariable.of("0.5")
										)
								), null
						), null
				)
		));
	}


	private static ConfiguredEngine<?> tornado(DataGenContext ctx) {
		double vsp = 0.5;
		int life = 20;
		double rate = Math.tan(10 * Mth.DEG_TO_RAD);
		double w = vsp * 180 / Math.PI / 2;
		double ir = 0.3;
		String radius = ir + "+TickCount*" + vsp * rate;
		String angle = w / (vsp * rate) + "*(log(" + radius + ")+log(" + ir + "))";
		return new ListLogic(List.of(
				new PredicateLogic(
						BooleanVariable.of("TickUsing>=10"),
						new ProcessorEngine(SelectionType.ENEMY,
								new LinearCubeSelector(
										IntVariable.of("6"),
										DoubleVariable.of("1.5")
								),
								List.of(
										new DamageProcessor(ctx.damage(DamageTypes.FREEZE),
												DoubleVariable.of("4"), true, true),
										new PushProcessor(
												DoubleVariable.of("0.1"),
												DoubleVariable.ZERO,
												DoubleVariable.ZERO,
												PushProcessor.Type.TO_CENTER
										)
								)), null),
				new DelayedIterator(
						IntVariable.of("10"),
						IntVariable.of("1"),
						new RandomVariableLogic("r", 1,
								new MoveEngine(List.of(
										new Dir2NormalModifier()
								), new RingRandomIterator(
										DoubleVariable.of(ir + ""),
										DoubleVariable.of(ir + ""),
										DoubleVariable.of("-180"),
										DoubleVariable.of("180"),
										IntVariable.of("3"),
										new MoveEngine(List.of(
												NormalOffsetModifier.of("rand(" + (-vsp) + "," + vsp + ")")
										), new CustomParticleInstance(
												DoubleVariable.of("0"),
												DoubleVariable.of("0.7"),
												IntVariable.of("" + life),
												true,
												new MovePosMotion(List.of(
														ForwardOffsetModifier.of("-" + ir),
														RotationModifier.of(angle),
														ForwardOffsetModifier.of(radius),
														new Normal2DirModifier(),
														ForwardOffsetModifier.of("TickCount*" + vsp)
												)),
												new SimpleParticleData(
														RenderTypePreset.NORMAL,
														ParticleTypes.SNOWFLAKE
												)
										)), null
								))
						), null
				)
		));
	}

}
