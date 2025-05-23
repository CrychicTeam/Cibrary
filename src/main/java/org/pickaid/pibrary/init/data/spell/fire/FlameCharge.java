package org.pickaid.pibrary.init.data.spell.fire;

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
import net.minecraft.world.item.Items;
import org.pickaid.pibrary.init.data.spell.UnrealHelper;

import java.util.List;

public class FlameCharge extends SpellDataGenEntry {

    public static final ResourceKey<InteractionAction> FLAME_CHARGE = spell("flame_charge");

    @Override
    public void register(BootstapContext<InteractionAction> ctx) {
        new InteractionAction(
                flameCharge(new DataGenContext(ctx)),
                Items.FIRE_CHARGE, 3300,
                InteractionActionCastType.CHARGE,
                InteractionActionTriggerType.FACING_FRONT
        ).verifyOnBuild(ctx, FLAME_CHARGE);
    }

    private static ConfiguredEngine<?> flameCharge(DataGenContext ctx) {
        return new PredicateLogic(BooleanVariable.of("Power==0"),
                new SphereRandomIterator(
                        DoubleVariable.of("2"),
                        IntVariable.of("50"),
                        new DustParticleInstance(
                                ColorVariable.Static.of(0xFF0000),
                                DoubleVariable.of("1"),
                                DoubleVariable.of("-.2"),
                                IntVariable.of("10")
                        ),
                        null
                ),
                new DelayedIterator(
                        IntVariable.of("min(2*TickUsing,80)"),
                        IntVariable.of("1"),
                        new MoveEngine(
                                List.of(
                                        new ToCurrentCasterPosModifier(),
                                        new SetDirectionModifier(
                                                DoubleVariable.ZERO,
                                                DoubleVariable.of("1"),
                                                DoubleVariable.ZERO
                                        ),
                                        new ForwardOffsetModifier(DoubleVariable.of("1")),
                                        new ToCurrentCasterDirModifier()
                                ),
                                new ListLogic(List.of(
                                        new ProcessorEngine(  // Push
                                                SelectionType.ALL,
                                                new SelfSelector(),
                                                List.of(
                                                        new PushProcessor(
                                                                DoubleVariable.of(".2"),
                                                                DoubleVariable.ZERO,
                                                                DoubleVariable.ZERO,
                                                                PushProcessor.Type.UNIFORM
                                                        )
                                                )
                                        ),
                                        new ProcessorEngine(  // Damage
                                                SelectionType.ENEMY,
                                                new ApproxCylinderSelector(
                                                        DoubleVariable.of("2"),
                                                        DoubleVariable.of("2")
                                                ),
                                                List.of(
                                                        new DamageProcessor(ctx.damage(DamageTypes.FIREBALL),
                                                                DoubleVariable.of("4"), true, true),
                                                        new KnockBackProcessor(
                                                                DoubleVariable.of("0.2"),
                                                                DoubleVariable.ZERO,
                                                                DoubleVariable.ZERO
                                                        )
                                                )
                                        ),
                                        new MoveEngine(  // Render
                                                List.of(
                                                        new ForwardOffsetModifier(DoubleVariable.of("-1"))
                                                ),
                                                UnrealHelper.cone(
                                                        1.5,
                                                        3,
                                                        30,
                                                        30,
                                                        new MoveEngine(
                                                                List.of(
                                                                        new ToCurrentCasterDirModifier()
                                                                ),
                                                                new DustParticleInstance(
                                                                        ColorVariable.Static.of(0xFF0000),
                                                                        DoubleVariable.of(".5"),
                                                                        DoubleVariable.of("1"),
                                                                        IntVariable.of("1")
                                                                )
                                                        )
                                                )
                                        )
                                ))
                        ),
                null
        )
        );
    }

}
