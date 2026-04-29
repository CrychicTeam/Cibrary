package org.pickaid.pibrary.api.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

/**
 * Minimal entity render bridge helper kept in Pibrary core.
 *
 * <p>Its job is limited to extraction plus thin-context forwarding. Engine
 * features such as layers, effects, backend adaptation, and visual registries
 * belong outside Pibrary.</p>
 */
public abstract class PiEntityRenderer<T extends Entity, R> extends EntityRenderer<T> {
    protected PiEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public final void render(
            T entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        R renderState = extract(entity, partialTick);
        renderState(entity, renderState, new PiEntityRenderContext(
                poseStack,
                bufferSource,
                partialTick,
                entityYaw,
                packedLight
        ));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    protected abstract R extract(T entity, float partialTick);

    protected abstract void renderState(T entity, R renderState, PiEntityRenderContext context);
}
