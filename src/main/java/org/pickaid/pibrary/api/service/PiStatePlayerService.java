package org.pickaid.pibrary.api.service;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Player-specialized stateful living service base class.
 *
 * @param <S> backing state type
 */
public abstract class PiStatePlayerService<S> extends PiStateLivingEntityService<S> {
    protected PiStatePlayerService(PiLivingServiceContext context) {
        super(context);
    }

    /**
     * Returns the owning player when available.
     *
     * @return owning player or {@code null}
     */
    public final @Nullable Player player() {
        return context().player();
    }
}
