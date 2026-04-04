package org.pickaid.pibrary.api.service;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public abstract class PiStatePlayerService<S> extends PiStateLivingEntityService<S> {
    protected PiStatePlayerService(PiLivingServiceContext context, Class<S> stateType) {
        super(context, stateType);
    }

    public final @Nullable Player player() {
        return context().player();
    }
}
