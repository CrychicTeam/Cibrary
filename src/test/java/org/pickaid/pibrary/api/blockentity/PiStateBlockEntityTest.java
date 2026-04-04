package org.pickaid.pibrary.api.blockentity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.dev.example.CounterState;

class PiStateBlockEntityTest {
    @Test
    void updateStateInvokesServerStateHookOnlyWhenStateChanges() {
        TestBlockEntity blockEntity = new TestBlockEntity();

        blockEntity.increment();
        blockEntity.assignSameCount();

        assertEquals(1, blockEntity.serverStateChangedCalls);
        assertEquals(1, blockEntity.viewState().count);
    }

    @Test
    void handleUpdateTagRefreshesClientRenderState() {
        TestBlockEntity source = new TestBlockEntity();
        TestBlockEntity target = new TestBlockEntity();
        source.increment();

        target.handleUpdateTag(source.getUpdateTag());

        assertEquals(1, target.viewState().count);
        assertEquals(1, target.renderRefreshCalls);
    }

    @Test
    void onDataPacketRefreshesClientRenderState() {
        TestBlockEntity source = new TestBlockEntity();
        TestBlockEntity target = new TestBlockEntity();
        source.increment();

        ClientboundBlockEntityDataPacket packet = createPacket(source.getUpdateTag());
        target.onDataPacket(null, packet);

        assertEquals(1, target.viewState().count);
        assertEquals(1, target.renderRefreshCalls);
    }

    private static ClientboundBlockEntityDataPacket createPacket(CompoundTag tag) {
        try {
            Constructor<ClientboundBlockEntityDataPacket> constructor =
                    ClientboundBlockEntityDataPacket.class.getDeclaredConstructor(BlockPos.class, BlockEntityType.class, CompoundTag.class);
            constructor.setAccessible(true);
            return constructor.newInstance(BlockPos.ZERO, null, tag);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Failed to construct ClientboundBlockEntityDataPacket for test", exception);
        }
    }

    private static final class TestBlockEntity extends PiStateBlockEntity<CounterState> {
        private int serverStateChangedCalls;
        private int renderRefreshCalls;

        private TestBlockEntity() {
            super(null, BlockPos.ZERO, null, CounterState.class);
        }

        private void increment() {
            updateState(state -> state.count++);
        }

        private void assignSameCount() {
            updateState(state -> state.count = state.count);
        }

        @Override
        protected void onServerStateChanged() {
            serverStateChangedCalls++;
            super.onServerStateChanged();
        }

        @Override
        public void requestModelDataUpdate() {
            renderRefreshCalls++;
        }
    }
}
