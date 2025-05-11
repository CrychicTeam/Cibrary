package org.pickaid.pibrary.tools.helper;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;

public class AdvancementUtils {

    public static boolean hasAdvancementUnlocked(Player player, Advancement advancement) {
        return player instanceof ServerPlayer
                && ((ServerPlayer) player).server.getPlayerList()
                .getPlayerAdvancements((ServerPlayer) player).getOrStartProgress(advancement).isDone();
    }

    public static Advancement getAdvancement(Dist dist, ResourceLocation resourceLocation) {
        if (dist.isClient()) {
            return getAdvancementManagerClient().getAdvancements().get(resourceLocation);
        }
        return getAdvancementManagerServer().getAdvancement(resourceLocation);
    }

    public static ServerAdvancementManager getAdvancementManagerServer() {
        return ServerLifecycleHooks.getCurrentServer().getAdvancements();
    }

    @OnlyIn(Dist.CLIENT)
    public static ClientAdvancements getAdvancementManagerClient() {
        if (Minecraft.getInstance().player != null) {
            return Minecraft.getInstance().player.connection.getAdvancements();
        }
        return null;
    }

    public static <T extends CriterionTrigger<?>> T registerCriteriaTrigger(T criterion) {
        return CriteriaTriggers.register(criterion);
    }
}
