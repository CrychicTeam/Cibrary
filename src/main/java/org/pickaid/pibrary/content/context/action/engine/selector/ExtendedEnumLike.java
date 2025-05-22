package org.pickaid.pibrary.content.context.action.engine.selector;

import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.pickaid.pibrary.content.context.action.engine.context.EngineContext;

public interface ExtendedEnumLike<T> {
    boolean test(Entity target, LivingEntity user);

    Iterable<Entity> select(ServerLevel sl, EngineContext ctx, AABB aabb);

    Codec<T> codec();
}