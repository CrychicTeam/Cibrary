package org.pickaid.pibrary.content.context.action.engine.processor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;
import org.pickaid.pibrary.content.context.action.engine.core.EntityProcessor;
import org.pickaid.pibrary.content.context.action.engine.core.ProcessorType;
import org.pickaid.pibrary.content.context.variable.DoubleVariable;
import org.pickaid.pibrary.init.LibraryObjects;
import org.pickaid.pibrary.init.LibraryRegistries;

import java.util.Collection;

public record TeleportProcessor(
        DoubleVariable x,
        DoubleVariable y,
        DoubleVariable z
) implements EntityProcessor<TeleportProcessor> {

    public static final Codec<TeleportProcessor> CODEC = RecordCodecBuilder.create(i -> i.group(
            DoubleVariable.codec("x", e -> e.x),
            DoubleVariable.codec("y", e -> e.y),
            DoubleVariable.codec("z", e -> e.z)
    ).apply(i, TeleportProcessor::new));

    @Override
    public ProcessorType<TeleportProcessor> type() {
        return LibraryObjects.TP.get();
    }

    @Override
    public void process(Collection<LivingEntity> le, EngineContext ctx) {
        double posX = x().eval(ctx);
        double posY = y().eval(ctx);
        double posZ = z().eval(ctx);
        for (var e : le) {
            e.teleportTo(posX, posY, posZ);
        }
    }
}
