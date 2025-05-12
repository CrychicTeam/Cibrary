package org.pickaid.pibrary.content.key.combo;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.key.KeyData;

public class ComboRegistrationSample {
    public static void registerCombos() {
        ComboSystem comboSystem = ComboSystem.getInstance();
        ResourceLocation advancedComboId = new ResourceLocation(Pibrary.MOD_ID, "advanced_combo");

        ResourceLocation keyId = new ResourceLocation("minecraft:attack");
        ResourceLocation keyId2 = new ResourceLocation("minecraft:forward");

        ComboDefinition.ComboRequirement[] advancedRequirements = {
                new ComboDefinition.ComboRequirement(keyId2, KeyData.KeyState.PRESSED),
                new ComboDefinition.ComboRequirement(keyId, KeyData.KeyState.PRESSED)
        };

        comboSystem.registerCombo(new ComboDefinition.StateCombo(
                advancedComboId,
                advancedRequirements,
                (comboId) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    if (minecraft.player != null) {
                        minecraft.player.displayClientMessage(
                                Component.literal("Executed advanced combo!"),
                                false
                        );
                        minecraft.player.sendSystemMessage(
                                Component.literal("Executed advanced combo!")
                        );
                    }
                }
        ));
    }
}