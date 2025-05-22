package org.pickaid.pibrary.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Optional;

public class Proxy {
    public Proxy() {
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static LocalPlayer getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    /** @deprecated */
    @Deprecated
    @Nullable
    public static Player getPlayer() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return getClientPlayer();
        } else {
            return null;
        }
    }

    /** @deprecated */
    @Deprecated
    @Nullable
    public static Level getWorld() {
        return switch (FMLEnvironment.dist) {
            case CLIENT -> getClientWorld();
            case DEDICATED_SERVER -> getServer().map(MinecraftServer::overworld).orElse(null);
        };
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public static ClientLevel getClientWorld() {
        return Minecraft.getInstance().level;
    }

    public static Optional<MinecraftServer> getServer() {
        return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer());
    }
}