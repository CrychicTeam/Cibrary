package org.pickaid.pibrary.api.facet;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Player-specialized stateful living facet base class.
 *
 * @param <S> backing state type
 */
public abstract class PiStatePlayerFacet<S> extends PiStateLivingEntityFacet<S> {
    protected PiStatePlayerFacet(PiLivingFacetContext context) {
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
