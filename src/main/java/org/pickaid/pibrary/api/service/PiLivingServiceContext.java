package org.pickaid.pibrary.api.service;

import java.util.Objects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class PiLivingServiceContext {
    private final @Nullable LivingEntity living;
    private final PiLivingServiceHost host;

    public PiLivingServiceContext(@Nullable LivingEntity living, PiLivingServiceHost host) {
        this.living = living;
        this.host = Objects.requireNonNull(host, "host");
    }

    public @Nullable LivingEntity living() {
        return living;
    }

    public @Nullable Player player() {
        return living instanceof Player player ? player : null;
    }

    public PiLivingServiceHost host() {
        return host;
    }
}
