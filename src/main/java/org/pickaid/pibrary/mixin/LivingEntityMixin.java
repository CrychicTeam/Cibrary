package org.pickaid.pibrary.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.effect.IPlayerEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "setSprinting", at = @At("HEAD"))
    private void cibrary$setSprintingHook(boolean pSprinting, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player) {
            for (Map.Entry<LivingEntity, IPlayerEffect> entry : Pibrary.activeEffects.entrySet()) {
                if (entry.getKey().equals(self)) {
                    if (player.isSprinting()) {
                        entry.getValue().startSprintingEffect(player);
                    } else {
                        entry.getValue().stopSprintingEffect(player);
                    }
                }
            }
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void cibrary$jumpFromGroundHook(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player) {
            for (Map.Entry<LivingEntity, IPlayerEffect> entry : Pibrary.activeEffects.entrySet()) {
                if (entry.getKey().equals(self)) {
                    if (player.isSprinting()) {
                        entry.getValue().sprintingJumpEffect(player);
                    } else {
                        entry.getValue().jumpEffect(player);
                    }
                }
            }
        }
    }

    @Inject(method = "checkFallDamage", at = @At("HEAD"))
    private void cibrary$checkFallDamageHook(double pY, boolean pOnGround, BlockState pState, BlockPos pPos, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof ServerPlayer player && pOnGround) {
            for (Map.Entry<LivingEntity, IPlayerEffect> entry : Pibrary.activeEffects.entrySet()) {
                if (entry.getKey().equals(self)) {
                    entry.getValue().landEffect(player, pY, pState, pPos);
                }
            }
        }
    }
}