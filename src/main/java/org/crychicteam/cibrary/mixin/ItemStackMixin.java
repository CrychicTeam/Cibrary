package org.crychicteam.cibrary.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.crychicteam.cibrary.content.event.ItemDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
    public <T extends LivingEntity> void onHurtAndBreak(int pAmount, T pEntity, Consumer<LivingEntity> pOnBroken, CallbackInfo ci) {
        ItemStack stack = (ItemStack)(Object)this;
        ItemDamageEvent event = new ItemDamageEvent(stack, pAmount, pEntity, pOnBroken);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
            return;
        }
        pAmount = event.getDamage();
    }
}