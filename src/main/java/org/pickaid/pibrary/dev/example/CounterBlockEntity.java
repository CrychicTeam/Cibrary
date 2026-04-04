package org.pickaid.pibrary.dev.example;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.pickaid.pibrary.api.blockentity.PiStateBlockEntity;

public final class CounterBlockEntity extends PiStateBlockEntity<CounterState> {
    public CounterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, CounterState.class);
    }

    public void increment() {
        updateState(state -> state.count++);
    }

    public int getCount() {
        return viewState().count;
    }
}
