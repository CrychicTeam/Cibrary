package org.pickaid.pibrary.content.key.combo;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.Pibrary;;
import org.pickaid.pibrary.content.key.DefaultKey;
import org.pickaid.pibrary.content.key.KeyData;

public class ComboRegistrationSample {
    public static void registerCombos() {
        ComboSystem comboSystem = ComboSystem.getInstance();
        ResourceLocation advancedComboId = new ResourceLocation(Pibrary.MOD_ID, "advanced_combo");

        ComboDefinition.ComboRequirement[] advancedRequirements = {
                new ComboDefinition.ComboRequirement(DefaultKey.FORWARD_KEY.id, KeyData.KeyState.PRESSED),
                new ComboDefinition.ComboRequirement(DefaultKey.ATTACK_KEY.id, KeyData.KeyState.PRESSED),
                new ComboDefinition.ComboRequirement(DefaultKey.FORWARD_KEY.id, KeyData.KeyState.PRESSED)
        };

        comboSystem.registerCombo(new ComboDefinition.StateCombo(
                advancedComboId,
                advancedRequirements,
                (comboId) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    if (minecraft.player != null) {
//                        minecraft.player.setDeltaMovement(minecraft.player.getLookAngle().x *2, minecraft.player.getLookAngle().y *2, minecraft.player.getLookAngle().z * 2);
                    }
                }
        ));
    }
}