package org.pickaid.pibrary.dev.example.client;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import org.pickaid.pibrary.dev.example.CounterBlockEntity;

@OnlyIn(Dist.CLIENT)
public final class CounterBlockEntityRenderers {
    private CounterBlockEntityRenderers() {
    }

    public static void register(EntityRenderersEvent.RegisterRenderers event, BlockEntityType<CounterBlockEntity> type) {
        event.registerBlockEntityRenderer(type, CounterBlockEntityRenderer::new);
    }
}
