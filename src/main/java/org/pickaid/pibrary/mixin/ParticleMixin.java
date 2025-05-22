package org.pickaid.pibrary.mixin;

import net.minecraft.client.particle.Particle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(Particle.class)
public abstract class ParticleMixin {
    @Unique
    private Function<Particle, Integer> pibrary$lightColorFunction;

    @Inject(method = "getLightColor", at = @At(value = "RETURN"), cancellable = true)
    private void pil$getLightColor(float pPartialTick, CallbackInfoReturnable<Integer> cir) {
        Particle self = (Particle) (Object) this;
        if (pibrary$lightColorFunction != null) {
            cir.setReturnValue(pibrary$lightColorFunction.apply(self));
        }
    }

    @Unique
    public void pil$setLightColorFunction(Function<Particle, Integer> lightColorFunction) {
        this.pibrary$lightColorFunction = lightColorFunction;
    }
}
