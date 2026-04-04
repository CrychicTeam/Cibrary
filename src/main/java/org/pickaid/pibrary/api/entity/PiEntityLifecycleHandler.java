package org.pickaid.pibrary.api.entity;

import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface PiEntityLifecycleHandler<E extends Entity> {
    default void onJoinLevel(E entity, Level level, boolean loadedFromDisk) {
    }

    default void onEnterSection(E entity, SectionPos oldSection, SectionPos newSection) {
    }
}
