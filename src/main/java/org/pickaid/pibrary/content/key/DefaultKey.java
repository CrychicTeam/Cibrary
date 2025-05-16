package org.pickaid.pibrary.content.key;

import net.minecraft.client.Minecraft;
import org.pickaid.pibrary.content.key.registry.KeyConfig;

public class DefaultKey {
    public static final KeyConfig FORWARD_KEY;
    public static final KeyConfig BACKWARD_KEY;
    public static final KeyConfig LEFT_KEY;
    public static final KeyConfig RIGHT_KEY;
    public static final KeyConfig ATTACK_KEY;

    static {
        FORWARD_KEY = new KeyConfig.Builder("minecraft", "forward", Minecraft.getInstance().options.keyUp)
                .build();
        BACKWARD_KEY = new KeyConfig.Builder("minecraft", "backward", Minecraft.getInstance().options.keyDown)
                .build();
        LEFT_KEY = new KeyConfig.Builder("minecraft", "left", Minecraft.getInstance().options.keyLeft)
                .build();
        RIGHT_KEY = new KeyConfig.Builder("minecraft", "right", Minecraft.getInstance().options.keyRight)
                .build();
        ATTACK_KEY = new KeyConfig.Builder("minecraft", "attack", Minecraft.getInstance().options.keyAttack)
                .enableRapidClick()
                .enableCharging()
                .setRapidClickTimeWindow(500)
                .build();
    }

    public static void register() {}
}