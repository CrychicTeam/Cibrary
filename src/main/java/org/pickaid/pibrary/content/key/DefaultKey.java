package org.pickaid.pibrary.content.key;

import net.minecraft.client.Minecraft;

public class DefaultKey {
    public static final ConfiguredKey FORWARD_KEY;
    public static final ConfiguredKey BACKWARD_KEY;
    public static final ConfiguredKey LEFT_KEY;
    public static final ConfiguredKey RIGHT_KEY;
    public static final ConfiguredKey ATTACK_KEY;

    static {
        FORWARD_KEY = KeyRegistry.register(new ConfiguredKey.Builder("minecraft", "forward", Minecraft.getInstance().options.keyUp)
                        .build());
        BACKWARD_KEY = KeyRegistry.register(new ConfiguredKey.Builder("minecraft", "backward", Minecraft.getInstance().options.keyDown)
                .build());
        LEFT_KEY = KeyRegistry.register(new ConfiguredKey.Builder("minecraft", "left", Minecraft.getInstance().options.keyLeft)
                .build());
        RIGHT_KEY = KeyRegistry.register(new ConfiguredKey.Builder("minecraft", "right", Minecraft.getInstance().options.keyRight)
                .build());
        ATTACK_KEY = KeyRegistry.register(new ConfiguredKey.Builder("minecraft", "attack", Minecraft.getInstance().options.keyAttack)
                .build());
    }

    public static void register() {}
}