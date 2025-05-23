package org.pickaid.pibrary.init.data.spell.ground;

import com.tterrag.registrate.providers.RegistrateLangProvider;
import org.pickaid.pibrary.content.context.action.engine.context.*;
import org.pickaid.pibrary.content.context.action.engine.core.*;
import org.pickaid.pibrary.content.context.action.engine.iterator.*;
import org.pickaid.pibrary.content.context.action.engine.logic.*;
import org.pickaid.pibrary.content.context.action.engine.modifier.*;
import org.pickaid.pibrary.content.context.action.engine.particle.*;
import org.pickaid.pibrary.content.context.action.engine.processor.*;
import org.pickaid.pibrary.content.context.action.engine.selector.*;
import org.pickaid.pibrary.content.context.action.engine.sound.SoundInstance;
import org.pickaid.pibrary.content.context.action.particle.engine.*;
import org.pickaid.pibrary.content.context.interaction.InteractionAction;
import org.pickaid.pibrary.content.context.interaction.InteractionActionCastType;
import org.pickaid.pibrary.content.context.interaction.InteractionActionTriggerType;
import org.pickaid.pibrary.content.context.variable.*;
import org.pickaid.pibrary.init.data.SpellDataGenEntry;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public class MagnetCore extends SpellDataGenEntry {

	public static final ResourceKey<InteractionAction> MAGNET_CORE = spell("magnet_core");

	@Override
	public void register(BootstapContext<InteractionAction> ctx) {
		new InteractionAction(
				magnetCore(new DataGenContext(ctx)),
				Blocks.IRON_ORE.asItem(), 3000,
				InteractionActionCastType.INSTANT,
				InteractionActionTriggerType.TARGET_POS
		).verifyOnBuild(ctx, MAGNET_CORE);
	}

    private static ConfiguredEngine<?> magnetCore(DataGenContext ctx) {
        return new ListLogic(List.of(
                new DelayedIterator(  // core
                        IntVariable.of("24"),
                        IntVariable.of("5"),
                        new SphereRandomIterator(
                                DoubleVariable.of("0.1+0.005*r"),
                                IntVariable.of("50"),
                                new DustParticleInstance(
                                        ColorVariable.Static.of(0xE88B00),
                                        DoubleVariable.of("2"),
                                        DoubleVariable.of(".01"),
                                        IntVariable.of("10")
                                ),
                                null
                        ),
                        "r"
                ),
                new LoopIterator(  // waves
                        IntVariable.of("6"),
                        new DelayLogic(
                                IntVariable.of("10+30*t-2*t*t"),  // 10 + (0-100), 6 waves
                                new SphereRandomIterator(
                                        DoubleVariable.of("8"),
                                        IntVariable.of("200"),
                                        new DustParticleInstance(
                                                ColorVariable.Static.of(0xE88B00),
                                                DoubleVariable.of("2"),
                                                DoubleVariable.of("-.5"),
                                                IntVariable.of("20-2*t")
                                        ),
                                        null
                                )
                        ),
                        "t"
                ),
                new DelayLogic(  // explode
                        IntVariable.of("120"),
                        new ListLogic(List.of(
                                new SphereRandomIterator(
                                        DoubleVariable.of(".01"),
                                        IntVariable.of("200"),
                                        new DustParticleInstance(
                                                ColorVariable.Static.of(0xE88B00),
                                                DoubleVariable.of("2"),
                                                DoubleVariable.of("3"),
                                                IntVariable.of("10")
                                        ),
                                        null
                                ),
                                new ProcessorEngine(
                                        SelectionType.ENEMY,
                                        new BoxSelector(
                                                DoubleVariable.of("8"),  // Only have range explode
                                                DoubleVariable.of("8"),
                                                true
                                        ),
                                        List.of(
                                                new DamageProcessor(
                                                        ctx.damage(DamageTypes.INDIRECT_MAGIC),
                                                        DoubleVariable.of("20"),
                                                        true, false
                                                ),
                                                new PushProcessor(
                                                        DoubleVariable.of("1"),
                                                        DoubleVariable.ZERO,
                                                        DoubleVariable.ZERO,
                                                        PushProcessor.Type.TO_CENTER
                                                )
                                        )
                                )
                        ))
                ),
                new DelayedIterator(  // tick damage
                        IntVariable.of("120"),
                        IntVariable.of("1"),
                        new ProcessorEngine(
                                SelectionType.ENEMY,
                                new BoxSelector(
                                        DoubleVariable.of("16"),  // full range drain
                                        DoubleVariable.of("16"),
                                        true
                                ),
                                List.of(
                                        new DamageProcessor(
                                                ctx.damage(DamageTypes.INDIRECT_MAGIC),
                                                DoubleVariable.of("1"),
                                                true, false
                                        ),
                                        new PushProcessor(
                                                DoubleVariable.of("-.1"),
                                                DoubleVariable.ZERO,
                                                DoubleVariable.ZERO,
                                                PushProcessor.Type.TO_CENTER
                                        )
                                )
                        ),
                        null
                )
        ));
    }

}
