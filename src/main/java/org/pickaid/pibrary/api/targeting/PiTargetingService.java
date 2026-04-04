package org.pickaid.pibrary.api.targeting;

import java.util.List;
import net.minecraft.world.entity.Entity;

public interface PiTargetingService {
    List<Entity> resolve(Entity caster, PiTargetQuery query);
}
