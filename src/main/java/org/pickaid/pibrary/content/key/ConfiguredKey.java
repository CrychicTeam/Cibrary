package org.pickaid.pibrary.content.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.settings.KeyConflictContext;

public class ConfiguredKey {
    public final ResourceLocation id;
    public final KeyMapping keyMapping;
    public final float maxPower;
    public final boolean enableCharging;
    public final boolean showDebugMessage;

    private ConfiguredKey(Builder builder) {
        this.id = builder.id;
        this.keyMapping = builder.existingKeyMapping != null ?
                builder.existingKeyMapping :
                new KeyMapping(
                        "key." + builder.id.getNamespace() + "." + builder.id.getPath(),
                        builder.type == null ? KeyConflictContext.IN_GAME : builder.type,
                        InputConstants.Type.KEYSYM,
                        builder.defaultKey,
                        "key.category." + builder.id.getNamespace() + "." + builder.category
                );
        this.maxPower = builder.maxPower;
        this.enableCharging = builder.enableCharging;
        this.showDebugMessage = builder.showDebugMessage;
    }

    public static class Builder {
        private final ResourceLocation id;
        private final int defaultKey;
        private KeyConflictContext type;
        private KeyMapping existingKeyMapping;
        private String category = "default";
        private float maxPower = 2.0f;
        private boolean enableCharging = false;
        private boolean showDebugMessage = false;

        public Builder(String modid, String keyId, int defaultKey) {
            this.id = new ResourceLocation(modid, keyId);
            this.defaultKey = defaultKey;
        }

        public Builder(String modid, String keyId, KeyMapping existingKey) {
            this.id = new ResourceLocation(modid, keyId);
            this.defaultKey = existingKey.getKey().getValue();
            this.existingKeyMapping = existingKey;
        }

        /**
         * set the category of this key.
         * <p/>
         * Showed on Vanilla Key Editor.
         * <p/>
         * default: "default"
         * @param category the category
         * @return {@link Builder}
         */
        public Builder category(String category) {
            this.category = category;
            return this;
        }

        /**
         * set the max power of charging.
         * @param maxPower >= 1.0f
         * @return {@link Builder}
         */
        public Builder maxPower(float maxPower) {
            this.maxPower = maxPower;
            return this;
        }

        /**
         * Whether to track charging for this key.
         * @return {@link Builder}
         */
        public Builder enableCharging() {
            this.enableCharging = true;
            return this;
        }

        /**
         * Whether to show debug message for this key.
         * </p>
         * Usually used in debugging when developing this system.
         * @return {@link Builder}
         */
        public Builder showDebugMessage() {
            this.showDebugMessage = true;
            return this;
        }

        /**
         * Build {@link ConfiguredKey} instance.
         * @return {@link ConfiguredKey}
         */
        public ConfiguredKey build() {
            return new ConfiguredKey(this);
        }
    }

    public boolean isDown() {
        return this.keyMapping.isDown();
    }
}