package org.pickaid.pibrary.dev.example.client;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import org.pickaid.pibrary.dev.example.CounterBlockEntity;

/**
 * Sample client registration helper for the counter block entity renderer.
 */
@OnlyIn(Dist.CLIENT)
public final class CounterBlockEntityRenderers {
    private CounterBlockEntityRenderers() {
    }

    /**
     * Registers the sample renderer.
     *
     * @param event Forge renderer registration event
     * @param type counter block entity type
     */
    public static void register(EntityRenderersEvent.RegisterRenderers event, BlockEntityType<CounterBlockEntity> type) {
        event.registerBlockEntityRenderer(type, CounterBlockEntityRenderer::new);
    }
}
