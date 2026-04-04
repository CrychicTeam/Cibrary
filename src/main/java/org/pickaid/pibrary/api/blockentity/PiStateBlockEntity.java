package org.pickaid.pibrary.api.blockentity;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.pickaid.pibrary.runtime.state.PiHostState;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.piserializekit.api.schema.PiDirtySet;

public abstract class PiStateBlockEntity<S> extends BlockEntity {
    private final PiHostState<S> host;

    protected PiStateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Class<S> stateType) {
        super(type, pos, state);
        this.host = new PiHostState<>(stateType);
    }

    public final S viewState() {
        return host.viewState();
    }

    protected final void updateState(Consumer<S> change) {
        if (host.updateState(change)) {
            onServerStateChanged();
        }
    }

    protected final PiDirtySet dirtySet() {
        return host.dirtySet();
    }

    protected final void clearDirty() {
        host.clearDirty();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.merge(host.saveFull());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        host.loadFull(tag, PiDecodeContext.strict());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return host.saveClientView();
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        host.applyDelta(tag, PiDecodeContext.strict());
        afterClientStateApplied();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, ignored -> host.saveClientView());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            host.applyDelta(tag, PiDecodeContext.strict());
            afterClientStateApplied();
        }
    }

    protected void onServerStateChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    protected void afterClientStateApplied() {
        requestModelDataUpdate();
    }
}
