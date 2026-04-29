package org.pickaid.pibrary.api.targeting;

import java.util.List;
import net.minecraft.world.entity.Entity;

/**
 * Resolves target queries into ordered entity lists.
 */
public interface PiTargetingService {
    /**
     * Resolves a targeting query for the given caster.
     *
     * @param caster casting entity
     * @param query immutable target query
     * @return immutable ordered list of matching entities
     */
    List<Entity> resolve(Entity caster, PiTargetQuery query);
}
