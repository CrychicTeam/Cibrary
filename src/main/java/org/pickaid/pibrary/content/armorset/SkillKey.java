package org.pickaid.pibrary.content.armorset;

import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.content.key.ConfiguredKey;
import org.pickaid.pibrary.content.key.KeyRegistry;
import org.lwjgl.glfw.GLFW;

public class SkillKey {
    public static ConfiguredKey SKILL_KEY;
    public static void init() {
        SKILL_KEY = KeyRegistry.register(new ConfiguredKey.Builder(Pibrary.MOD_ID, "armor_set_skill", GLFW.GLFW_KEY_G)
                .category("armor_set")
                .enableCharging()
                .maxPower(4.0f)
                .build()
        );
    }
}