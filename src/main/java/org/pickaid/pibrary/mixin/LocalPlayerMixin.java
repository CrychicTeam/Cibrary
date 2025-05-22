package org.pickaid.pibrary.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.pickaid.pibrary.init.LibraryTags;
import org.pickaid.pibrary.tools.utils.raytrace.FastItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
    @Shadow
    private boolean startedUsingItem;

    @WrapOperation( method = {"aiStep", "canStartSprinting"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z")} )
    private boolean pickAIDLibrary$aiStep_fastUseItem(LocalPlayer player, Operation<Boolean> op) {
        if (this.startedUsingItem) {
            ItemStack stack = player.getUseItem();
            Item item = stack.getItem();
            if (item instanceof FastItem fast) {
                if (fast.isFast(stack)) {
                    return false;
                }
                if (stack.is(LibraryTags.FAST_USE)) {
                    return false;
                }
            }
        }
        return op.call(player);
    }
}
