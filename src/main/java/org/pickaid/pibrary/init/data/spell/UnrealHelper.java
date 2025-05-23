package org.pickaid.pibrary.init.data.spell;

import org.pickaid.pibrary.content.context.action.engine.core.ConfiguredEngine;
import org.pickaid.pibrary.content.context.action.engine.iterator.LoopIterator;
import org.pickaid.pibrary.content.context.action.engine.iterator.RingIterator;
import org.pickaid.pibrary.content.context.action.engine.logic.MoveEngine;
import org.pickaid.pibrary.content.context.action.engine.modifier.Dir2NormalModifier;
import org.pickaid.pibrary.content.context.action.engine.modifier.ForwardOffsetModifier;
import org.pickaid.pibrary.content.context.variable.*;

import java.util.List;

public class UnrealHelper {
    public static ConfiguredEngine<?> cone(double radius, double height, int num, int layers, ConfiguredEngine<?> child) {
        return new LoopIterator(
                IntVariable.of(Integer.toString(layers)),
                new MoveEngine(
                        List.of(
                                new ForwardOffsetModifier(
                                        DoubleVariable.of(height/(layers-1) + "*cone_l")
                                ),
                                new Dir2NormalModifier()
                        ),
                        new RingIterator(
                                DoubleVariable.of(radius/(layers-1) + String.format("*(%d-cone_l)", layers)),
                                DoubleVariable.of("0"),
                                DoubleVariable.of("360"),
                                IntVariable.of(1D*num/(layers-1) + String.format("*(%d-cone_l)", layers)),
                                false,
                                child,
                                null
                        )
                ),
                "cone_l"
        );
    }
}
