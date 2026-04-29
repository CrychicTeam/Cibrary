package org.pickaid.pibrary.api.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Thin entity render bridge context kept in Pibrary core.
 *
 * <p>This record keeps the values already known at the entity render
 * entrypoint so source-side helpers can stay backend-neutral.</p>
 */
public record PiEntityRenderContext(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        float partialTick,
        float entityYaw,
        int packedLight
) {
    public PiEntityRenderContext {
        Objects.requireNonNull(poseStack, "poseStack");
        Objects.requireNonNull(bufferSource, "bufferSource");
    }
}
