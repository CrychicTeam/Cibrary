package org.pickaid.pibrary.content.events.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.pickaid.pibrary.content.key.KeyData;
import org.pickaid.pibrary.content.key.KeyStateMachine;
import org.pickaid.pibrary.api.key.KeyStateMachineManager;
import org.pickaid.pibrary.content.key.combo.ComboSystem;
import org.pickaid.pibrary.content.key.registry.KeyConfig;
import org.pickaid.pibrary.content.key.registry.KeyRegistry;

import java.util.Optional;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientKeyHandler {
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event) {
        if (event.phase != Phase.END || Minecraft.getInstance().player == null) return;
        long currentTime = System.currentTimeMillis();

        for (KeyConfig config : KeyRegistry.getAllConfigs()) {
            KeyStateMachine stateMachine = KeyStateMachineManager.getOrCreate(config);

            KeyData.KeyState oldState = stateMachine.getKeyData().state;
            stateMachine.process(config.isDown(), currentTime);
            KeyData.KeyState newState = stateMachine.getKeyData().state;

            if (oldState != newState) {
                ComboSystem.getInstance().recordKeyInput(
                        config.id,
                        currentTime,
                        newState
                );
            }
        }
    }

    public static Optional<KeyData> getKeyState(ResourceLocation keyId) {
        return KeyStateMachineManager.getStateMachine(keyId)
                .map(KeyStateMachine::getKeyData);
    }

    public static void setKeyState(KeyData keyData) {
        KeyStateMachineManager.getStateMachine(keyData.keyId)
                .ifPresent(stateMachine -> {
                    stateMachine.forceState(keyData.state);
                    stateMachine.sendNetworkUpdate();

                    ComboSystem.getInstance().recordKeyInput(
                            keyData.keyId,
                            System.currentTimeMillis(),
                            keyData.state
                    );
                });
    }
}