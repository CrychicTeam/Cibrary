package org.crychicteam.cibrary.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.crychicteam.cibrary.Cibrary;
import org.crychicteam.cibrary.content.armorset.ArmorSet;
import org.crychicteam.cibrary.content.armorset.common.ArmorSetManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    private final ArmorSetManager cibrary$armorSetManager = Cibrary.ARMOR_SET_MANAGER;

    @Inject(method = "setSprinting", at = @At("HEAD"))
    private void cibrary$setSprintingHook(boolean pSprinting, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player) {
            ArmorSet activeSet = cibrary$armorSetManager.getActiveArmorSet(player);
            if (pSprinting) {
                activeSet.getEffect().startSprintingEffect(player);
            } else {
                activeSet.getEffect().stopSprintingEffect(player);
            }
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void cibrary$jumpFromGroundHook(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player) {
            ArmorSet activeSet = cibrary$armorSetManager.getActiveArmorSet(player);
            if (player.isSprinting()) {
                activeSet.getEffect().sprintingJumpEffect(player);
            } else {
                activeSet.getEffect().jumpEffect(player);
            }
        }
    }

    @Inject(method = "checkFallDamage", at = @At("HEAD"))
    private void cibrary$checkFallDamageHook(double pY, boolean pOnGround, BlockState pState, BlockPos pPos, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player && pOnGround) {
            ArmorSet activeSet = cibrary$armorSetManager.getActiveArmorSet(player);
            activeSet.getEffect().landEffect(player, player.fallDistance, pState, pPos);
        }
    }
}