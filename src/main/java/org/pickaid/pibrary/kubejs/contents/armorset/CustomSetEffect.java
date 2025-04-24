package org.pickaid.pibrary.kubejs.contents.armorset;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ItemStackedOnOtherEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import org.pickaid.pibrary.api.event.ItemHurtEffectResult;
import org.pickaid.pibrary.api.event.StandOnFluidEvent;
import org.pickaid.pibrary.content.armorset.ISetEffect;
import org.pickaid.pibrary.content.key.KeyData;

/**
 * Custom Set Effect implementation for KubeJS integration.
 */
public class CustomSetEffect implements ISetEffect {
    private final Builder builder;
    private final String identifier;

    public CustomSetEffect(Builder builder) {
        this.builder = builder;
        this.identifier = builder.identifier;
    }

    @Override
    public void applyEffect(LivingEntity entity) {
        if (builder.applyEffectCallback != null) {
            builder.applyEffectCallback.apply(entity);
        }
    }

    @Override
    public void removeEffect(LivingEntity entity) {
        if (builder.removeEffectCallback != null) {
            builder.removeEffectCallback.apply(entity);
        }
    }

    @Override
    public void skillEffect(LivingEntity entity) {
        if (builder.skillEffectCallback != null) {
            builder.skillEffectCallback.apply(entity);
        } else {
            ISetEffect.super.skillEffect(entity);
        }
    }

    @Override
    public void releaseEffect(LivingEntity entity) {
        if (builder.releaseEffectCallback != null) {
            builder.releaseEffectCallback.apply(entity);
        } else {
            ISetEffect.super.releaseEffect(entity);
        }
    }

    @Override
    public ItemHurtEffectResult itemHurtEffect(LivingEntity entity, ItemStack item, int originalDamage) {
        if (builder.itemHurtEffectCallback != null) {
            return builder.itemHurtEffectCallback.apply(entity, item, originalDamage);
        }
        return ISetEffect.super.itemHurtEffect(entity, item, originalDamage);
    }

    @Override
    public void startSprintingEffect(LivingEntity entity) {
        if (builder.startSprintingEffectCallback != null) {
            builder.startSprintingEffectCallback.apply(entity);
        } else {
            ISetEffect.super.startSprintingEffect(entity);
        }
    }

    @Override
    public void stopSprintingEffect(LivingEntity entity) {
        if (builder.stopSprintingEffectCallback != null) {
            builder.stopSprintingEffectCallback.apply(entity);
        } else {
            ISetEffect.super.stopSprintingEffect(entity);
        }
    }

    @Override
    public void sprintingEffect(LivingEntity entity) {
        if (builder.sprintingEffectCallback != null) {
            builder.sprintingEffectCallback.apply(entity);
        } else {
            ISetEffect.super.sprintingEffect(entity);
        }
    }

    @Override
    public void normalTickingEffect(LivingEntity entity) {
        if (builder.normalTickingEffectCallback != null) {
            builder.normalTickingEffectCallback.apply(entity);
        } else {
            ISetEffect.super.normalTickingEffect(entity);
        }
    }

    @Override
    public void jumpEffect(LivingEntity entity) {
        if (builder.jumpEffectCallback != null) {
            builder.jumpEffectCallback.apply(entity);
        } else {
            ISetEffect.super.jumpEffect(entity);
        }
    }

    @Override
    public void landEffect(LivingEntity entity, double distance, BlockState landingBlock, BlockPos pos) {
        if (builder.landEffectCallback != null) {
            builder.landEffectCallback.apply(entity, distance, landingBlock, pos);
        } else {
            ISetEffect.super.landEffect(entity, distance, landingBlock, pos);
        }
    }

    @Override
    public void sprintingJumpEffect(LivingEntity entity) {
        if (builder.sprintingJumpEffectCallback != null) {
            builder.sprintingJumpEffectCallback.apply(entity);
        } else {
            ISetEffect.super.sprintingJumpEffect(entity);
        }
    }

    @Override
    public void onTargetedEffect(LivingEntity entity, LivingChangeTargetEvent changer) {
        if (builder.onTargetedEffectCallback != null) {
            builder.onTargetedEffectCallback.apply(entity, changer);
        } else {
            ISetEffect.super.onTargetedEffect(entity, changer);
        }
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public void blockingEffect(LivingEntity entity) {
        if (builder.blockingEffectCallback != null) {
            builder.blockingEffectCallback.apply(entity);
        } else {
            ISetEffect.super.blockingEffect(entity);
        }
    }

    @Override
    public void crouchingEffect(LivingEntity entity) {
        if (builder.crouchingEffectCallback != null) {
            builder.crouchingEffectCallback.apply(entity);
        } else {
            ISetEffect.super.crouchingEffect(entity);
        }
    }

    @Override
    public void onStandOnFluidEffect(LivingEntity entity, StandOnFluidEvent event) {
        if (builder.onStandOnFluidEffectCallback != null) {
            builder.onStandOnFluidEffectCallback.apply(entity, event);
        } else {
            ISetEffect.super.onStandOnFluidEffect(entity, event);
        }
    }

    @Override
    public void stackedOnOther(LivingEntity entity, ItemStackedOnOtherEvent event) {
        if (builder.stackedOnOtherCallback != null) {
            builder.stackedOnOtherCallback.apply(entity, event);
        } else {
            ISetEffect.super.stackedOnOther(entity, event);
        }
    }

    @Override
    public void onSkillPress(ServerPlayer player, KeyData keyData) {
        if (builder.onSkillPressCallback != null) {
            builder.onSkillPressCallback.apply(player, keyData);
        } else {
            ISetEffect.super.onSkillPress(player, keyData);
        }
    }

    @Override
    public void onSkillCharging(ServerPlayer player, KeyData power) {
        if (builder.onSkillChargingCallback != null) {
            builder.onSkillChargingCallback.apply(player, power);
        } else {
            ISetEffect.super.onSkillCharging(player, power);
        }
    }

    @Override
    public void onSkillRelease(ServerPlayer player, KeyData power) {
        if (builder.onSkillReleaseCallback != null) {
            builder.onSkillReleaseCallback.apply(player, power);
        } else {
            ISetEffect.super.onSkillRelease(player, power);
        }
    }

    @Override
    public Component getDescription(Player player, ItemStack stack) {
        if (builder.getDescriptionCallback != null) {
            return builder.getDescriptionCallback.getDescription(player, stack);
        }
        return ISetEffect.super.getDescription(player, stack);
    }

    @Override
    public boolean checkDescription(Player player, ItemStack stack) {
        if (builder.checkDescriptionCallback != null) {
            return builder.checkDescriptionCallback.checkDescription(player, stack);
        }
        return ISetEffect.super.checkDescription(player, stack);
    }

    @Info("Callback interface for handling apply effect events")
    public interface ApplyEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling remove effect events")
    public interface RemoveEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling skill effect events")
    public interface SkillEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling release effect events")
    public interface ReleaseEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling item hurt effect events")
    public interface ItemHurtEffectCallback {
        ItemHurtEffectResult apply(LivingEntity entity, ItemStack item, int originalDamage);
    }

    @Info("Callback interface for handling start sprinting effect events")
    public interface StartSprintingEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling stop sprinting effect events")
    public interface StopSprintingEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling sprinting effect events")
    public interface SprintingEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling normal ticking effect events")
    public interface NormalTickingEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling jump effect events")
    public interface JumpEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling land effect events")
    public interface LandEffectCallback {
        void apply(LivingEntity entity, double distance, BlockState landingBlock, BlockPos pos);
    }

    @Info("Callback interface for handling sprinting jump effect events")
    public interface SprintingJumpEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling targeted effect events")
    public interface OnTargetedEffectCallback {
        void apply(LivingEntity entity, LivingChangeTargetEvent changer);
    }

    @Info("Callback interface for handling blocking effect events")
    public interface BlockingEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling crouching effect events")
    public interface CrouchingEffectCallback {
        void apply(LivingEntity entity);
    }

    @Info("Callback interface for handling stand on fluid effect events")
    public interface OnStandOnFluidEffectCallback {
        void apply(LivingEntity entity, StandOnFluidEvent event);
    }

    @Info("Callback interface for handling stacked on other events")
    public interface StackedOnOtherCallback {
        void apply(LivingEntity entity, ItemStackedOnOtherEvent event);
    }

    @Info("Callback interface for handling skill press events")
    public interface OnSkillPressCallback {
        void apply(ServerPlayer player, KeyData keyData);
    }

    @Info("Callback interface for handling skill charging events")
    public interface OnSkillChargingCallback {
        void apply(ServerPlayer player, KeyData power);
    }

    @Info("Callback interface for handling skill release events")
    public interface OnSkillReleaseCallback {
        void apply(ServerPlayer player, KeyData power);
    }

    public interface GetDescriptionCallback {
        Component getDescription(Player player, ItemStack itemStack);
    }

    public interface CheckDescriptionCallback {
        boolean checkDescription(Player player, ItemStack itemStack);
    }

    public static class Builder {
        private String identifier;

        private GetDescriptionCallback getDescriptionCallback;
        private CheckDescriptionCallback checkDescriptionCallback;

        private ApplyEffectCallback applyEffectCallback;
        private RemoveEffectCallback removeEffectCallback;
        private SkillEffectCallback skillEffectCallback;
        private ReleaseEffectCallback releaseEffectCallback;
        private ItemHurtEffectCallback itemHurtEffectCallback;
        private StartSprintingEffectCallback startSprintingEffectCallback;
        private StopSprintingEffectCallback stopSprintingEffectCallback;
        private SprintingEffectCallback sprintingEffectCallback;
        private NormalTickingEffectCallback normalTickingEffectCallback;
        private JumpEffectCallback jumpEffectCallback;
        private LandEffectCallback landEffectCallback;
        private SprintingJumpEffectCallback sprintingJumpEffectCallback;
        private OnTargetedEffectCallback onTargetedEffectCallback;
        private BlockingEffectCallback blockingEffectCallback;
        private CrouchingEffectCallback crouchingEffectCallback;
        private OnStandOnFluidEffectCallback onStandOnFluidEffectCallback;
        private StackedOnOtherCallback stackedOnOtherCallback;
        private OnSkillPressCallback onSkillPressCallback;
        private OnSkillChargingCallback onSkillChargingCallback;
        private OnSkillReleaseCallback onSkillReleaseCallback;

        public Builder(String identifier) {
            this.identifier = identifier;
        }

        @Info("Sets the unique identifier for this set effect")
        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        @Info("Sets a callback that triggers when the armor set is applied to an entity")
        public Builder applyEffect(ApplyEffectCallback callback) {
            this.applyEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the armor set is removed from an entity")
        public Builder removeEffect(RemoveEffectCallback callback) {
            this.removeEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity uses the armor set's skill")
        public Builder skillEffect(SkillEffectCallback callback) {
            this.skillEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity finishes using an item with this armor set")
        public Builder releaseEffect(ReleaseEffectCallback callback) {
            this.releaseEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when entity's armors/curios/items in hands are damaged")
        public Builder itemHurtEffect(ItemHurtEffectCallback callback) {
            this.itemHurtEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity starts sprinting")
        public Builder startSprintingEffect(StartSprintingEffectCallback callback) {
            this.startSprintingEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity stops sprinting")
        public Builder stopSprintingEffect(StopSprintingEffectCallback callback) {
            this.stopSprintingEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers every tick while the entity is sprinting")
        public Builder sprintingEffect(SprintingEffectCallback callback) {
            this.sprintingEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers every tick while the entity exists")
        public Builder normalTickingEffect(NormalTickingEffectCallback callback) {
            this.normalTickingEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity jumps")
        public Builder jumpEffect(JumpEffectCallback callback) {
            this.jumpEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity lands")
        public Builder landEffect(LandEffectCallback callback) {
            this.landEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity jumps while sprinting")
        public Builder sprintingJumpEffect(SprintingJumpEffectCallback callback) {
            this.sprintingJumpEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity is targeted by another entity")
        public Builder onTargetedEffect(OnTargetedEffectCallback callback) {
            this.onTargetedEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity is blocking")
        public Builder blockingEffect(BlockingEffectCallback callback) {
            this.blockingEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity is sneaking/crouching")
        public Builder crouchingEffect(CrouchingEffectCallback callback) {
            this.crouchingEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity is standing on a fluid")
        public Builder onStandOnFluidEffect(OnStandOnFluidEffectCallback callback) {
            this.onStandOnFluidEffectCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when items are stacked on other items")
        public Builder stackedOnOther(StackedOnOtherCallback callback) {
            this.stackedOnOtherCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity presses the skill key")
        public Builder onSkillPress(OnSkillPressCallback callback) {
            this.onSkillPressCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity is charging a skill")
        public Builder onSkillCharging(OnSkillChargingCallback callback) {
            this.onSkillChargingCallback = callback;
            return this;
        }

        @Info("Sets a callback that triggers when the entity releases a skill")
        public Builder onSkillRelease(OnSkillReleaseCallback callback) {
            this.onSkillReleaseCallback = callback;
            return this;
        }

        public Builder getDescription(GetDescriptionCallback callback) {
            this.getDescriptionCallback = callback;
            return this;
        }

        public Builder checkDescription(CheckDescriptionCallback callback) {
            this.checkDescriptionCallback = callback;
            return this;
        }

        public CustomSetEffect build() {
            return new CustomSetEffect(this);
        }
    }
}