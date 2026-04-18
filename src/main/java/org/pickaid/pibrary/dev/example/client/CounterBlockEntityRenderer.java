package org.pickaid.pibrary.dev.example.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.pickaid.pibrary.api.presentation.PiPresentations;
import org.pickaid.pibrary.api.render.PiRenderContext;
import org.pickaid.pibrary.api.render.blockentity.PiBlockEntityRenderer;
import org.pickaid.pibrary.dev.example.CounterBlockEntity;
import org.pickaid.pibrary.dev.example.CounterWorldRenderVisual;

/**
 * Host-focused example renderer that resolves a typed world-render projection
 * without turning Pibrary into the full render runtime.
 */
@OnlyIn(Dist.CLIENT)
public final class CounterBlockEntityRenderer
        extends PiBlockEntityRenderer<CounterBlockEntity, CounterWorldRenderVisual> {
    private final Font font;

    public CounterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.font = Minecraft.getInstance().font;
    }

    @Override
    protected CounterWorldRenderVisual extract(CounterBlockEntity blockEntity, float partialTick) {
        return PiPresentations.worldRender().resolve(blockEntity, CounterWorldRenderVisual.class, partialTick);
    }

    @Override
    protected void renderState(
            CounterBlockEntity blockEntity,
            CounterWorldRenderVisual renderState,
            PiRenderContext context
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        PoseStack poseStack = context.poseStack();

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.1D, 0.5D);
        poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Matrix4f matrix = poseStack.last().pose();
        float x = -font.width(renderState.text()) / 2.0F;
        font.drawInBatch(
                renderState.text(),
                x,
                0.0F,
                renderState.color(),
                false,
                matrix,
                context.bufferSource(),
                Font.DisplayMode.NORMAL,
                0,
                context.packedLight()
        );
        poseStack.popPose();
    }
}
