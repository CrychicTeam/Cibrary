package org.pickaid.pibrary.tools.compat.ponder.instruction;

import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.TickingInstruction;
import net.minecraft.util.Mth;

public class CameraShakeInstruction extends TickingInstruction {
    private float originalXRotation;
    private float originalYRotation;

    private final float intensity;
    private final float frequency;
    private final boolean affectBothAxes;

    public CameraShakeInstruction(int duration) {
        this(1.0f, 0.8f, true, duration);
    }

    public CameraShakeInstruction(float intensity, float frequency, boolean affectBothAxes, int duration) {
        super(false, duration);
        this.intensity = intensity;
        this.frequency = frequency;
        this.affectBothAxes = affectBothAxes;
        originalXRotation = 0f;
        originalYRotation = 0f;
    }

    @Override
    protected void firstTick(PonderScene scene) {
        super.firstTick(scene);
        originalXRotation = scene.getTransform().xRotation.getChaseTarget();
        originalYRotation = scene.getTransform().yRotation.getChaseTarget();
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        float progress = (float) remainingTicks / totalTicks;
        float envelopeIntensity = intensity * 4 * progress * (1 - progress);
        float xShake = envelopeIntensity * 5f * (
                Mth.sin(remainingTicks * 0.9f * frequency) +
                        0.5f * Mth.sin(remainingTicks * 1.4f * frequency) +
                        0.25f * Mth.sin(remainingTicks * 2.3f * frequency)
        );
        scene.getTransform().xRotation.chase(originalXRotation + xShake, 1f, LerpedFloat.Chaser.EXP);
        if (affectBothAxes) {
            float yShake = envelopeIntensity * 3f * (
                    Mth.sin(remainingTicks * 0.7f * frequency + 0.4f) +
                            0.4f * Mth.sin(remainingTicks * 1.6f * frequency + 0.7f)
            );
            scene.getTransform().yRotation.chase(originalYRotation + yShake, 1f, LerpedFloat.Chaser.EXP);
        }
    }
}