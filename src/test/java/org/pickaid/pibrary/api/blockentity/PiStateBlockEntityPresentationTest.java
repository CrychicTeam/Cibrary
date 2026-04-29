package org.pickaid.pibrary.api.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.presentation.PiPresentationContext;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentationScope;
import org.pickaid.pibrary.api.presentation.PiPresentations;
import org.pickaid.pibrary.dev.example.CounterState;
import org.pickaid.pibrary.dev.example.CounterWorldRenderVisual;

class PiStateBlockEntityPresentationTest {
    @Test
    void clientApplyInvalidatesCachedWorldRenderProjection() {
        TestBlockEntity blockEntity = new TestBlockEntity();
        TestBlockEntity source = new TestBlockEntity();
        source.setCount(4);

        CounterWorldRenderVisual first =
                PiPresentations.worldRender().resolve(blockEntity, CounterWorldRenderVisual.class, 0.0F);
        blockEntity.handleUpdateTag(source.getUpdateTag());

        CounterWorldRenderVisual second =
                PiPresentations.worldRender().resolve(blockEntity, CounterWorldRenderVisual.class, 0.0F);

        assertEquals("0", first.text());
        assertEquals("4", second.text());
    }

    private static final class TestBlockEntity extends PiStateBlockEntity<CounterState> implements PiPresentationSource {
        private TestBlockEntity() {
            super(null, BlockPos.ZERO, null, CounterState.class);
        }

        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.worldRender().snapshot(
                    CounterWorldRenderVisual.class,
                    PiPresentationScope.TRACKING,
                    partialTick -> CounterWorldRenderVisual.from(viewState())
            );
            context.worldRender().refreshOnClientApply(CounterWorldRenderVisual.class);
        }

        private void setCount(int count) {
            updateState(state -> state.count = count);
        }
    }
}
