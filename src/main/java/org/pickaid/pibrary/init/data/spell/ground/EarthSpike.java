package org.pickaid.pibrary.init.data.spell.ground;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.block.Blocks;
import org.pickaid.pibrary.content.context.action.engine.context.DataGenContext;
import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.iterator.DelayedIterator;
import org.pickaid.pibrary.content.context.action.engine.iterator.RingIterator;
import org.pickaid.pibrary.content.context.action.engine.logic.*;
import org.pickaid.pibrary.content.context.action.engine.modifier.ForwardOffsetModifier;
import org.pickaid.pibrary.content.context.action.engine.modifier.SetDirectionModifier;
import org.pickaid.pibrary.content.context.action.engine.modifier.SetNormalModifier;
import org.pickaid.pibrary.content.context.action.engine.particle.DustParticleInstance;
import org.pickaid.pibrary.content.context.action.engine.processor.DamageProcessor;
import org.pickaid.pibrary.content.context.action.engine.processor.KnockBackProcessor;
import org.pickaid.pibrary.content.context.action.engine.selector.ApproxCylinderSelector;
import org.pickaid.pibrary.content.context.action.engine.selector.SelectionType;
import org.pickaid.pibrary.content.context.interaction.InteractionAction;
import org.pickaid.pibrary.content.context.interaction.InteractionActionCastType;
import org.pickaid.pibrary.content.context.interaction.InteractionActionTriggerType;
import org.pickaid.pibrary.init.data.SpellDataGenEntry;
import org.pickaid.pibrary.content.context.variable.*;
import org.pickaid.pibrary.init.data.spell.UnrealHelper;

import java.util.List;

public class EarthSpike extends SpellDataGenEntry {

    public static final ResourceKey<InteractionAction> EARTH_SPIKE = spell("earth_spike");
    public static final ResourceKey<InteractionAction> EARTH_SPIKE_FIELD = spell("earth_spike_field");

    @Override
    public void register(BootstapContext<InteractionAction> ctx) {
        new InteractionAction(
                earthSpike(new DataGenContext(ctx)),
                Blocks.POINTED_DRIPSTONE.asItem(), 3200,
                InteractionActionCastType.INSTANT,
                InteractionActionTriggerType.TARGET_POS
        ).verifyOnBuild(ctx, EARTH_SPIKE);
        new InteractionAction(
                earthSpikeField(new DataGenContext(ctx)),
                Blocks.POINTED_DRIPSTONE.asItem(), 3300,
                InteractionActionCastType.INSTANT,
                InteractionActionTriggerType.TARGET_POS
        ).verifyOnBuild(ctx, EARTH_SPIKE_FIELD);
    }

    private static ConfiguredEngine<?> earthSpike(DataGenContext ctx) {
        return new ListLogic(List.of(
                new ProcessorEngine(
                        SelectionType.ENEMY,
                        new ApproxCylinderSelector(
                                DoubleVariable.of("1.5"),
                                DoubleVariable.of("3")
                        ),
                        List.of(
                                new DamageProcessor(ctx.damage(DamageTypes.INDIRECT_MAGIC),
                                        DoubleVariable.of("8"), true, true),
                                new KnockBackProcessor(
                                        DoubleVariable.of("0.2"),
                                        DoubleVariable.ZERO,
                                        DoubleVariable.ZERO
                                )
                        )
                ),
                new MoveEngine(  // Render
                        List.of(
                                new SetDirectionModifier(
                                        DoubleVariable.ZERO,
                                        DoubleVariable.of("1"),
                                        DoubleVariable.ZERO
                                ),
                                new ForwardOffsetModifier(DoubleVariable.of("-2"))
                        ),
                        new ListLogic(List.of(
                                new DelayedIterator(
                                        IntVariable.of("10"),
                                        IntVariable.of("1"),
                                        new MoveEngine(
                                                List.of(new ForwardOffsetModifier(DoubleVariable.of("0.2*t"))),
                                                UnrealHelper.cone(
                                                        .5,
                                                        2,
                                                        40,
                                                        20,
                                                        new DustParticleInstance(
                                                                ColorVariable.Static.of(0xE88B00),
                                                                DoubleVariable.of(".5"),
                                                                DoubleVariable.of(".01"),
                                                                IntVariable.of("1")
                                                        )
                                                )
                                        ),
                                        "t"
                                ),
                                new DelayLogic(
                                        IntVariable.of("11"),
                                        new MoveEngine(
                                                List.of(new ForwardOffsetModifier(DoubleVariable.of("2"))),
                                                UnrealHelper.cone(
                                                        .5,
                                                        2,
                                                        40,
                                                        20,
                                                        new DustParticleInstance(
                                                                ColorVariable.Static.of(0xE88B00),
                                                                DoubleVariable.of(".5"),
                                                                DoubleVariable.ZERO,
                                                                IntVariable.of("10")
                                                        )
                                                )
                                        )
                                )
                        ))
                )
        ));
    }

    private static ConfiguredEngine<?> earthSpikeField(DataGenContext ctx) {
        return new DelayedIterator(
                IntVariable.of("5"),
                IntVariable.of("10"),
                new PredicateLogic(
                        BooleanVariable.of("c>0"),
                        new MoveEngine(
                                List.of(new SetNormalModifier(DoubleVariable.ZERO, DoubleVariable.of("1"), DoubleVariable.ZERO)),
                                new RingIterator(
                                        DoubleVariable.of("2*c"),
                                        DoubleVariable.of("0"),
                                        DoubleVariable.of("360"),
                                        IntVariable.of("6*c"),
                                        false,
                                        earthSpike(ctx),
                                        null
                                )
                        ),
                        earthSpike(ctx)
                ),
                "c"
        );
    }
}

