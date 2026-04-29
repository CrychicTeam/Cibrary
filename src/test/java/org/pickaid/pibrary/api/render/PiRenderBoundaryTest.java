package org.pickaid.pibrary.api.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.blaze3d.vertex.PoseStack;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.render.blockentity.PiBlockEntityRenderer;
import org.pickaid.pibrary.api.render.entity.PiEntityRenderContext;

class PiRenderBoundaryTest {
    private static final MultiBufferSource BUFFER = renderType -> {
        throw new UnsupportedOperationException("Buffer access is not needed for this boundary test");
    };

    @Test
    void blockEntityRenderHelperOnlyExtractsAndPassesBridgeContext() {
        TestRenderer renderer = new TestRenderer();
        PoseStack poseStack = new PoseStack();

        renderer.render(null, 0.5F, poseStack, BUFFER, 12, 34);

        assertEquals(1, renderer.extractCalls);
        assertEquals(1, renderer.renderCalls);
        assertSame(poseStack, renderer.lastContext.poseStack());
        assertSame(BUFFER, renderer.lastContext.bufferSource());
        assertEquals(0.5F, renderer.lastContext.partialTick());
        assertEquals(12, renderer.lastContext.packedLight());
        assertEquals(34, renderer.lastContext.packedOverlay());
        assertEquals("state", renderer.lastState);
    }

    @Test
    void entityRenderContextRemainsThinValueCarrier() {
        PoseStack poseStack = new PoseStack();
        PiEntityRenderContext context = new PiEntityRenderContext(poseStack, BUFFER, 0.25F, 90.0F, 15);

        assertSame(poseStack, context.poseStack());
        assertSame(BUFFER, context.bufferSource());
        assertEquals(0.25F, context.partialTick());
        assertEquals(90.0F, context.entityYaw());
        assertEquals(15, context.packedLight());
    }

    @Test
    void renderReadmeStatesThatWorldRenderConsumesPresentationCore() throws Exception {
        String readme = Files.readString(Path.of("src/main/java/org/pickaid/pibrary/api/render/README.md"));

        assertTrue(readme.contains("presentation core"));
        assertTrue(readme.contains("world_render"));
        assertTrue(readme.contains("PiPresentations.worldRender()"));
    }

    private static final class TestRenderer extends PiBlockEntityRenderer<BlockEntity, String> {
        private int extractCalls;
        private int renderCalls;
        private String lastState;
        private PiRenderContext lastContext;

        private TestRenderer() {
            super(null);
        }

        @Override
        protected String extract(BlockEntity blockEntity, float partialTick) {
            extractCalls++;
            return "state";
        }

        @Override
        protected void renderState(BlockEntity blockEntity, String renderState, PiRenderContext context) {
            renderCalls++;
            lastState = renderState;
            lastContext = context;
        }
    }
}
