package org.pickaid.pibrary.runtime.state;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.piserializekit.api.schema.PiDirtySet;
import org.pickaid.piserializekit.api.schema.PiFieldDescriptor;
import org.pickaid.piserializekit.api.schema.PiProjection;
import org.pickaid.piserializekit.api.schema.PiStateBinding;
import org.pickaid.piserializekit.api.schema.PiStateSnapshot;
import org.pickaid.piserializekit.runtime.schema.registry.PiSchemas;

/**
 * Generic generated-state wrapper that tracks dirty fields and provides full, filtered,
 * client-view, and delta serialization helpers.
 *
 * @param <S> backing state type
 */
public final class PiFacetState<S> {
    private final S state;
    private final PiStateBinding<S> binding;
    private final PiDirtySet dirtySet = new PiDirtySet();
    private PiStateSnapshot snapshot;

    /**
     * Creates a facet state wrapper from the generated schema for the given state type.
     *
     * @param stateType backing state type
     */
    public PiFacetState(Class<S> stateType) {
        this.binding = PiSchemas.require(Objects.requireNonNull(stateType, "stateType"));
        this.state = binding.newState();
        this.snapshot = binding.snapshot(state);
    }

    /**
     * Returns the mutable backing state view.
     *
     * @return backing state
     */
    public S viewState() {
        return state;
    }

    /**
     * Returns the schema id backing this facet state.
     *
     * @return schema id
     */
    public ResourceLocation schemaId() {
        return binding.schemaId();
    }

    /**
     * Applies a state mutation and marks every field whose serialized value changed.
     *
     * @param change state mutation callback
     * @return {@code true} when any field changed
     */
    public boolean updateState(Consumer<S> change) {
        change.accept(state);
        var changedBits = binding.diff(state, snapshot);
        if (changedBits.isEmpty()) {
            return false;
        }
        for (PiFieldDescriptor field : binding.fields()) {
            if (changedBits.contains(field)) {
                dirtySet.mark(field);
            }
        }
        snapshot = binding.snapshot(state);
        return true;
    }

    /**
     * Serializes the full state.
     *
     * @return full serialized state
     */
    public CompoundTag saveFull() {
        return binding.saveFull(state);
    }

    /**
     * Serializes the persisted subset of the state.
     *
     * @return persisted state payload
     */
    public CompoundTag savePersisted() {
        return binding.savePersisted(state);
    }

    /**
     * Serializes the full state and removes fields rejected by the filter.
     *
     * @param filter field visibility filter
     * @return filtered full state
     */
    public CompoundTag saveFiltered(Predicate<PiFieldDescriptor> filter) {
        PiProjection projection = filter::test;
        return saveProjection(projection);
    }

    /**
     * Serializes the state using the supplied projection.
     *
     * @param projection field projection
     * @return projected full state
     */
    public CompoundTag saveProjection(PiProjection projection) {
        return binding.saveProjection(state, projection);
    }

    /**
     * Loads the full state and clears dirty flags.
     *
     * @param tag serialized state
     * @param context decode context
     */
    public void loadFull(CompoundTag tag, PiDecodeContext context) {
        binding.loadFull(state, tag, context);
        dirtySet.clear();
        snapshot = binding.snapshot(state);
    }

    /**
     * Loads the persisted subset and clears dirty flags.
     *
     * @param tag persisted state payload
     * @param context decode context
     */
    public void loadPersisted(CompoundTag tag, PiDecodeContext context) {
        binding.loadPersisted(state, tag, context);
        dirtySet.clear();
        snapshot = binding.snapshot(state);
    }

    /**
     * Serializes the generated client-visible subset.
     *
     * @return client-visible state payload
     */
    public CompoundTag saveClientView() {
        return binding.saveClientView(state);
    }

    /**
     * Serializes a delta containing all dirty fields.
     *
     * @return delta payload
     */
    public CompoundTag writeDelta() {
        return binding.writeDelta(state, dirtySet);
    }

    /**
     * Serializes a delta containing only dirty fields accepted by the filter.
     *
     * @param filter field visibility filter
     * @return filtered delta payload
     */
    public CompoundTag writeDelta(Predicate<PiFieldDescriptor> filter) {
        PiProjection projection = filter::test;
        return writeDelta(projection);
    }

    /**
     * Serializes a delta containing only dirty fields accepted by the projection.
     *
     * @param projection field projection
     * @return projected delta payload
     */
    public CompoundTag writeDelta(PiProjection projection) {
        return binding.writeDelta(state, dirtySet, projection);
    }

    /**
     * Applies a delta to the current state.
     *
     * @param tag delta payload
     * @param context decode context
     */
    public void applyDelta(CompoundTag tag, PiDecodeContext context) {
        binding.applyDelta(state, tag, context);
        snapshot = binding.snapshot(state);
    }

    /**
     * Returns whether any dirty field matches the supplied filter.
     *
     * @param filter field visibility filter
     * @return {@code true} when at least one matching dirty field exists
     */
    public boolean hasDirty(Predicate<PiFieldDescriptor> filter) {
        PiProjection projection = filter::test;
        return hasDirty(projection);
    }

    /**
     * Returns whether any dirty field matches the supplied projection.
     *
     * @param projection field projection
     * @return {@code true} when at least one matching dirty field exists
     */
    public boolean hasDirty(PiProjection projection) {
        for (PiFieldDescriptor field : binding.fields()) {
            if (projection.includes(field) && dirtySet.contains(field)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the dirty-set tracker.
     *
     * @return dirty-set tracker
     */
    public PiDirtySet dirtySet() {
        return dirtySet;
    }

    /**
     * Clears every dirty flag.
     */
    public void clearDirty() {
        dirtySet.clear();
    }

    /**
     * Clears dirty flags accepted by the supplied filter.
     *
     * @param filter field visibility filter
     */
    public void clearDirty(Predicate<PiFieldDescriptor> filter) {
        PiProjection projection = filter::test;
        clearDirty(projection);
    }

    /**
     * Clears dirty flags accepted by the supplied projection.
     *
     * @param projection field projection
     */
    public void clearDirty(PiProjection projection) {
        for (PiFieldDescriptor field : binding.fields()) {
            if (projection.includes(field)) {
                dirtySet.clear(field);
            }
        }
    }
}
