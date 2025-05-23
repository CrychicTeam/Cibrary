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
import org.pickaid.pibrary.content.context.action.engine.sound.SoundInstance;
import org.pickaid.pibrary.content.context.action.particle.engine.*;
import org.pickaid.pibrary.content.context.interaction.InteractionAction;
import org.pickaid.pibrary.content.context.interaction.InteractionActionCastType;
import org.pickaid.pibrary.content.context.interaction.InteractionActionTriggerType;
import org.pickaid.pibrary.content.context.variable.*;
import org.pickaid.pibrary.init.data.SpellDataGenEntry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.Items;

import java.util.List;

public class IcyFlash extends SpellDataGenEntry {

    public static final ResourceKey<InteractionAction> ICY_FLASH = spell("icy_flash");

    @Override
    public void register(BootstapContext<InteractionAction> ctx) {
        new InteractionAction(
                icyFlash(new DataGenContext(ctx)),
                Items.SNOWBALL.asItem(), 3100,
                InteractionActionCastType.INSTANT,
                InteractionActionTriggerType.TARGET_POS
        ).verifyOnBuild(ctx, ICY_FLASH);
    }

    private static ConfiguredEngine<?> icyFlash(DataGenContext ctx) {
        return new ListLogic(List.of(
                new ProcessorEngine(  // TP
                        SelectionType.ALL,
                        new SelfSelector(),
                        List.of(
                                new TeleportProcessor(
                                        DoubleVariable.of("PosX"),
                                        DoubleVariable.of("PosY"),
                                        DoubleVariable.of("PosZ")
                                )
                        )
                ),
                new SoundInstance(  // Sound
                        SoundEvents.ENDERMAN_TELEPORT,
                        DoubleVariable.of("2"),
                        DoubleVariable.ZERO
                ),
                new ProcessorEngine(  // Damage
                        SelectionType.ENEMY,
                        new ApproxCylinderSelector(
                                DoubleVariable.of("1"),
                                DoubleVariable.of("2")
                        ),
                        List.of(
                                new DamageProcessor(ctx.damage(DamageTypes.FREEZE),
                                        DoubleVariable.of("4"), true, true),
                                new KnockBackProcessor(
                                        DoubleVariable.of("0.1"),
                                        DoubleVariable.ZERO,
                                        DoubleVariable.ZERO
                                )
                        )
                ),
                new LoopIterator(  // Render
                        IntVariable.of("100"),
                        new MoveEngine(
                                List.of(
                                        new RandomOffsetModifier(
                                                RandomOffsetModifier.Type.RECT,
                                                DoubleVariable.of("2"),
                                                DoubleVariable.of("2"),
                                                DoubleVariable.of("2")
                                        ),
                                        new SetDirectionModifier(
                                                DoubleVariable.ZERO,
                                                DoubleVariable.of("-1"),
                                                DoubleVariable.ZERO
                                        )
                                ),
                                new SimpleParticleInstance(
                                        ParticleTypes.SNOWFLAKE,
                                        DoubleVariable.of("0.1")
                                )
                        ),
                        null
                )
        ));
    }
}
