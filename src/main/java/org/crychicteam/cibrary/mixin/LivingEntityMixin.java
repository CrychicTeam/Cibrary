package org.crychicteam.cibrary.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.capability.ArmorSetCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "setSprinting", at = @At("HEAD"))
    private void setSprintingHook(boolean pSprinting, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer && pSprinting) {
            ServerPlayer player = (ServerPlayer) self;
            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(cap -> {
                ArmorSet activeSet = cap.getActiveSet();
                activeSet.getEffect().startSprintingEffect(player);
            });
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void jumpFromGroundHook(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) self;
            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(cap -> {
                ArmorSet activeSet = cap.getActiveSet();
                if (player.isSprinting()) {
                    activeSet.getEffect().sprintingJumpEffect(player);
                } else {
                    activeSet.getEffect().jumpEffect(player);
                }
            });
        }
    }

    @Inject(method = "checkFallDamage", at = @At("HEAD"))
    private void checkFallDamageHook(double pY, boolean pOnGround, BlockState pState, BlockPos pPos, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer && pOnGround) {
            ServerPlayer player = (ServerPlayer) self;
            player.getCapability(ArmorSetCapability.ARMOR_SET_CAPABILITY).ifPresent(cap -> {
                ArmorSet activeSet = cap.getActiveSet();
                activeSet.getEffect().landEffect(player, pY, pState, pPos);
            });
        }
    }
}