package org.pickaid.pibrary.api.service;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Optional lifecycle hook for services that need custom clone handling.
 */
public interface PiCloneAwareLivingService {
    /**
     * Called after state has been copied from the original entity into the clone.
     *
     * @param original source entity when available
     * @param wasDeath whether the clone was created by death/respawn
     */
    void onCloned(@Nullable LivingEntity original, boolean wasDeath);
}
