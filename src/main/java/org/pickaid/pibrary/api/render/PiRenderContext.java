package org.pickaid.pibrary.api.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Objects;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Thin block-entity render bridge context kept in Pibrary core.
 *
 * <p>This record only carries caller-provided values from the host-side render
 * entrypoint. It intentionally does not own backend routing, layers, or any
 * other render-engine policy.</p>
 */
public record PiRenderContext(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        float partialTick,
        int packedLight,
        int packedOverlay
) {
    public PiRenderContext {
        Objects.requireNonNull(poseStack, "poseStack");
        Objects.requireNonNull(bufferSource, "bufferSource");
    }
}
