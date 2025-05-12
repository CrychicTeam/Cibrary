package org.pickaid.pibrary.content.key.registry;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class KeyConfig {
    public final ResourceLocation id;
    public final KeyMapping keyMapping;
    public final boolean showDebugMessage;
    public final String category;
    public final long pressTimeTolerance;
    public final boolean enableCharging;
    public final float maxPower;
    public final boolean autoReleaseOnMax;
    public final long chargingTimeTolerance;
    public final boolean enableRapidClick;
    public final boolean disableNormalClick;
    public final int maxRapidClickCount;
    public final long rapidClickTimeWindow;
    public final long physicalReleaseDelay;
    public final long pressCooldown;
    public final long releaseCooldown;
    public final long rapidClickCompleteCooldown;
    public final long rapidClickResetCooldown;
    public final long timeoutTime;

    private KeyConfig(Builder builder) {
        this.id = builder.id;
        this.keyMapping = builder.existingKeyMapping != null ?
                builder.existingKeyMapping : createKeyMapping(builder);
        this.showDebugMessage = builder.showDebugMessage;
        this.category = builder.category;
        this.pressTimeTolerance = builder.pressTimeTolerance;
        this.enableCharging = builder.enableCharging;
        this.maxPower = Math.max(builder.maxPower, 1.0f);
        this.autoReleaseOnMax = builder.autoReleaseOnMax;
        this.chargingTimeTolerance = Math.max(builder.chargingTimeTolerance, 50);
        this.enableRapidClick = builder.enableRapidClick;
        this.disableNormalClick = builder.disableNormalClick;
        this.maxRapidClickCount = Math.max(2, builder.maxRapidClickCount); // At least 2 clicks needed
        this.rapidClickTimeWindow = Math.max(100, builder.rapidClickTimeWindow);
        this.physicalReleaseDelay = builder.physicalReleaseDelay;
        this.pressCooldown = builder.pressCooldown;
        this.releaseCooldown = builder.releaseCooldown;
        this.rapidClickCompleteCooldown = builder.rapidClickCompleteCooldown;
        this.rapidClickResetCooldown = builder.rapidClickResetCooldown;
        this.timeoutTime = Math.max(1000, builder.timeoutTime);
    }

    private KeyMapping createKeyMapping(Builder builder) {
        // Format translation key
        String translationKey = "key." + builder.id.getNamespace() + "." + builder.id.getPath();
        String categoryKey = "key.category." + builder.id.getNamespace() + "." + builder.category;

        return new KeyMapping(
                translationKey,
                builder.type != null ? builder.type : KeyConflictContext.IN_GAME,
                builder.keyModifier,
                InputConstants.Type.KEYSYM,
                builder.defaultKey,
                categoryKey
        );
    }

    public boolean isDown() {
        return keyMapping.isDown();
    }

    public static class Builder {
        public final ResourceLocation id;
        public final int defaultKey;
        public KeyMapping existingKeyMapping = null;
        public KeyConflictContext type = null;
        public String category = "default";
        public KeyModifier keyModifier = KeyModifier.NONE;
        public boolean showDebugMessage = false;
        public long pressTimeTolerance = 50L;
        public boolean enableCharging = false;
        public float maxPower = 2.0f;
        public boolean autoReleaseOnMax = true;
        public long chargingTimeTolerance = 100L;
        public boolean enableRapidClick = false;
        public boolean disableNormalClick = false;
        public int maxRapidClickCount = 5;
        public long rapidClickTimeWindow = 300L;
        public long physicalReleaseDelay = 200L;
        public long pressCooldown = 0L;
        public long releaseCooldown = 0L;
        public long rapidClickCompleteCooldown = 0L;
        public long rapidClickResetCooldown = 0L;
        public long timeoutTime = 5000L;

        public Builder(String modid, String keyId, int defaultKey) {
            this.id = new ResourceLocation(modid, keyId);
            this.defaultKey = defaultKey;
        }

        public Builder(String modid, String keyId, KeyMapping existingKey) {
            this.id = new ResourceLocation(modid, keyId);
            this.defaultKey = existingKey.getKey().getValue();
            this.existingKeyMapping = existingKey;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder showDebugMessage() {
            this.showDebugMessage = true;
            return this;
        }

        public Builder setKeyModifier(KeyModifier keyModifier) {
            this.keyModifier = keyModifier;
            return this;
        }

        public Builder setPressTimeTolerance(long milliseconds) {
            this.pressTimeTolerance = milliseconds;
            return this;
        }

        public Builder enableCharging() {
            this.enableCharging = true;
            return this;
        }

        public Builder maxPower(float maxPower) {
            this.maxPower = maxPower;
            return this;
        }

        public Builder autoReleaseOnMax(boolean autoRelease) {
            this.autoReleaseOnMax = autoRelease;
            return this;
        }

        public Builder setChargingTimeTolerance(long milliseconds) {
            this.chargingTimeTolerance = milliseconds;
            return this;
        }

        public Builder enableRapidClick() {
            this.enableRapidClick = true;
            return this;
        }

        public Builder disableNormalClick() {
            this.disableNormalClick = true;
            return this;
        }

        public Builder setMaxRapidClickCount(int count) {
            this.maxRapidClickCount = count;
            return this;
        }

        public Builder setRapidClickTimeWindow(long milliseconds) {
            this.rapidClickTimeWindow = milliseconds;
            return this;
        }

        public Builder setPhysicalReleaseDelay(long milliseconds) {
            this.physicalReleaseDelay = milliseconds;
            return this;
        }

        public Builder setPressCooldown(long milliseconds) {
            this.pressCooldown = milliseconds;
            return this;
        }

        public Builder setReleaseCooldown(long milliseconds) {
            this.releaseCooldown = milliseconds;
            return this;
        }

        public Builder setRapidClickCompleteCooldown(long milliseconds) {
            this.rapidClickCompleteCooldown = milliseconds;
            return this;
        }

        public Builder setRapidClickResetCooldown(long milliseconds) {
            this.rapidClickResetCooldown = milliseconds;
            return this;
        }

        public Builder setTimeoutTime(long milliseconds) {
            this.timeoutTime = milliseconds;
            return this;
        }

        public KeyConfig build() {
            return new KeyConfig(this);
        }
    }
}