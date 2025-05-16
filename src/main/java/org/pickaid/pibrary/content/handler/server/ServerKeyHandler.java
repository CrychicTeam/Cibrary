package org.pickaid.pibrary.content.handler.server;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.pickaid.pibrary.api.event.ConfiguredKeyEvent;
import org.pickaid.pibrary.content.key.KeyData;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber
public class ServerKeyHandler {
    private static ServerKeyHandler INSTANCE;
    public final Map<UUID, Map<ResourceLocation, KeyData>> playerKeyStates = new ConcurrentHashMap<>();

    /**
     * Retrieves the singleton instance of ServerKeyHandler.
     *
     * @return the ServerKeyHandler instance.
     */
    public static ServerKeyHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ServerKeyHandler();
        }
        return INSTANCE;
    }

    /**
     * Updates the key state for a specific player and key.
     *
     * @param player The player whose key state is being updated.
     * @param keyData The key data to be stored.
     */
    public void updateKeyState(ServerPlayer player, KeyData keyData) {
        playerKeyStates
                .computeIfAbsent(player.getUUID(), k -> new ConcurrentHashMap<>())
                .put(keyData.keyId, keyData);
    }

    /**
     * Clears all stored key states for a player when they log out.
     *
     * @param player The player whose states should be cleared.
     */
    public void clearPlayerStates(ServerPlayer player) {
        playerKeyStates.remove(player.getUUID());
    }

    /**
     * Handles player logout handler, clearing key states upon logout.
     *
     * @param event The player logout event.
     */
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            getInstance().clearPlayerStates(player);
        }
    }

//    @SubscribeEvent
//    public static void onKeyPressed(ConfiguredKeyEvent.Pressed event) {
//        if (!Objects.equals(event.getKeyData().keyId, new ResourceLocation("minecraft:attack"))) return;
//        event.getPlayer().sendSystemMessage(Component.literal("Key '" + event.getKeyData().keyId + "' pressed").withStyle(ChatFormatting.DARK_GREEN));
//    }
//
//    @SubscribeEvent
//    public static void onKeyCharging(ConfiguredKeyEvent.Charging event) {
//        if (!Objects.equals(event.getKeyData().keyId, new ResourceLocation("minecraft:attack"))) return;
//        event.getPlayer().sendSystemMessage(Component.literal("Key '" + event.getKeyData().keyId + "' is charging").withStyle(ChatFormatting.BLUE));
//    }
//
//    @SubscribeEvent
//    public static void onKeyRapidClick(ConfiguredKeyEvent.RapidClick event) {
//        if (!Objects.equals(event.getKeyData().keyId, new ResourceLocation("minecraft:attack"))) return;
//        event.getPlayer().sendSystemMessage(Component.literal("Key '" + event.getKeyData().keyId + "' performed a rapid click").withStyle(ChatFormatting.RED));
//    }
//
//    @SubscribeEvent
//    public static void onKeyRapidFinish(ConfiguredKeyEvent.RapidClickFinish event) {
//        if (!Objects.equals(event.getKeyData().keyId, new ResourceLocation("minecraft:attack"))) return;
//        event.getPlayer().sendSystemMessage(Component.literal("Key '" + event.getKeyData().keyId + "' rapid click finish").withStyle(ChatFormatting.YELLOW));
//    }
//
//    @SubscribeEvent
//    public static void onKeyRelease(ConfiguredKeyEvent.Released event) {
//        if (!Objects.equals(event.getKeyData().keyId, new ResourceLocation("minecraft:attack"))) return;
//        event.getPlayer().sendSystemMessage(Component.literal("Key '" + event.getKeyData().keyId + "' released").withStyle(ChatFormatting.GREEN));
//    }
//
//    @SubscribeEvent
//    public static void onKeyFinish(ConfiguredKeyEvent.Finished event) {
//        if (!Objects.equals(event.getKeyData().keyId, new ResourceLocation("minecraft:attack"))) return;
//        event.getPlayer().sendSystemMessage(Component.literal("Key '" + event.getKeyData().keyId + "' finish").withStyle(ChatFormatting.AQUA));
//    }
//
//    @SubscribeEvent
//    public static void onKeyHeld(ConfiguredKeyEvent.Held event) {
//        if (!Objects.equals(event.getKeyData().keyId, new ResourceLocation("minecraft:attack"))) return;
//        event.getPlayer().sendSystemMessage(Component.literal("Key '" + event.getKeyData().keyId + "' is held").withStyle(ChatFormatting.GOLD));
//    }
//
//    @SubscribeEvent
//    public static void onKeyHeldFinish(ConfiguredKeyEvent.HeldReleased event) {
//        if (!Objects.equals(event.getKeyData().keyId, new ResourceLocation("minecraft:attack"))) return;
//        event.getPlayer().sendSystemMessage(Component.literal("Key '" + event.getKeyData().keyId + "' is not held anymore").withStyle(ChatFormatting.GRAY));
//    }
//
//    @SubscribeEvent
//    public static void onKeyTimeOut(ConfiguredKeyEvent.TimeOut event) {
//        if (!Objects.equals(event.getKeyData().keyId, new ResourceLocation("minecraft:attack"))) return;
//        event.getPlayer().sendSystemMessage(Component.literal("Key '" + event.getKeyData().keyId + "' timed out").withStyle(ChatFormatting.RED));
//    }
}