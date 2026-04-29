package org.pickaid.pibrary.api.blockentity;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentations;
import org.pickaid.pibrary.api.menu.PiMenuData;
import org.pickaid.pibrary.api.menu.PiMenuDataSlot;
import org.pickaid.pibrary.runtime.state.PiFacetState;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.piserializekit.api.schema.PiDirtySet;

/**
 * Block entity base class backed by a PiSerializeKit state schema and automatic
 * vanilla update-packet integration.
 *
 * @param <S> backing state type
 */
public abstract class PiStateBlockEntity<S> extends BlockEntity {
    /**
     * NBT tag used for persistent and sync state payloads.
     */
    public static final String STATE_TAG = "__pi_state";

    private final PiFacetState<S> facetState;

    protected PiStateBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Class<S> stateType) {
        super(type, pos, state);
        this.facetState = new PiFacetState<>(stateType);
    }

    /**
     * Returns the mutable backing state view.
     *
     * @return backing state
     */
    public final S viewState() {
        return facetState.viewState();
    }

    /**
     * Applies a state mutation and pushes a vanilla block update when the state changes.
     *
     * @param change state mutation callback
     */
    protected final void updateState(Consumer<S> change) {
        if (facetState.updateState(change)) {
            onServerStateChanged();
        }
    }

    /**
     * Returns the dirty-set tracker owned by the backing facet state.
     *
     * @return dirty-set tracker
     */
    protected final PiDirtySet dirtySet() {
        return facetState.dirtySet();
    }

    /**
     * Clears all accumulated dirty flags.
     */
    protected final void clearDirty() {
        facetState.clearDirty();
    }

    /**
     * Creates vanilla menu data bound to this block entity.
     *
     * <p>If this block entity contributes presentation data, client-side menu
     * updates invalidate screen projections marked with
     * {@code refreshOnMenuData(...)}.</p>
     *
     * @param slots menu data slots
     * @return container data bridge
     */
    protected final PiMenuData menuData(PiMenuDataSlot... slots) {
        return this instanceof PiPresentationSource source
                ? PiMenuData.refreshing(source, slots)
                : PiMenuData.create(slots);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(STATE_TAG, facetState.savePersisted());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        facetState.loadPersisted(readStateTag(tag), PiDecodeContext.strict());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.put(STATE_TAG, facetState.saveClientView());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        facetState.applyDelta(readStateTag(tag), PiDecodeContext.strict());
        afterClientStateApplied();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, ignored -> {
            CompoundTag tag = new CompoundTag();
            tag.put(STATE_TAG, facetState.saveClientView());
            return tag;
        });
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            facetState.applyDelta(readStateTag(tag), PiDecodeContext.strict());
            afterClientStateApplied();
        }
    }

    /**
     * Called after server-side state changes. The default implementation marks the
     * block entity dirty and sends a vanilla client update packet.
     */
    protected void onServerStateChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Called after client-side state is applied from update tags or packets.
     * The default implementation refreshes model data.
     */
    protected void afterClientStateApplied() {
        if (this instanceof PiPresentationSource presentationSource) {
            PiPresentations.invalidateClientApply(presentationSource);
        }
        requestModelDataUpdate();
    }

    private static CompoundTag readStateTag(CompoundTag root) {
        if (root.contains(STATE_TAG, Tag.TAG_COMPOUND)) {
            return root.getCompound(STATE_TAG);
        }
        return root;
    }
}
