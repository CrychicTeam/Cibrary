package org.pickaid.pibrary.api.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.pickaid.piserializekit.api.schema.PiDirtySet;
import org.pickaid.piserializekit.api.schema.PiFieldKey;

public abstract class PiGeneratedBlockEntity extends BlockEntity {
    private final PiDirtySet dirtySet = new PiDirtySet();

    protected PiGeneratedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected final void dirty(PiFieldKey... keys) {
        for (PiFieldKey key : keys) {
            dirtySet.mark(key);
        }
    }

    protected final PiDirtySet dirtySet() {
        return dirtySet;
    }
}
