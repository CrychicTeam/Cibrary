package org.pickaid.pibrary.content.context.action.engine.context;

import org.pickaid.pibrary.content.context.action.engine.helper.Scheduler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record UserContext(Level level, LivingEntity user, @Nullable Scheduler scheduler) {

}
