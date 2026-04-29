package org.pickaid.pibrary.api.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.pickaid.pibrary.api.render.PiRenderContext;

/**
 * Minimal block-entity render bridge helper kept in Pibrary core.
 *
 * <p>This type separates source-side state extraction from drawing while
 * Pibrary owns the sync and refresh boundary.</p>
 */
public abstract class PiBlockEntityRenderer<T extends BlockEntity, R> implements BlockEntityRenderer<T> {
    protected PiBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public final void render(
            T blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        R renderState = extract(blockEntity, partialTick);
        renderState(blockEntity, renderState, new PiRenderContext(
                poseStack,
                bufferSource,
                partialTick,
                packedLight,
                packedOverlay
        ));
    }

    protected abstract R extract(T blockEntity, float partialTick);

    protected abstract void renderState(T blockEntity, R renderState, PiRenderContext context);
}
