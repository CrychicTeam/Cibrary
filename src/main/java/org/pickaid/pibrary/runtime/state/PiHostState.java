package org.pickaid.pibrary.runtime.state;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.pickaid.piserializekit.api.schema.PiDecodeContext;
import org.pickaid.piserializekit.api.schema.PiDirtySet;
import org.pickaid.piserializekit.api.schema.PiFieldDescriptor;
import org.pickaid.piserializekit.api.schema.PiStateBinding;
import org.pickaid.piserializekit.runtime.schema.PiSchemas;

public final class PiHostState<S> {
    private final S state;
    private final PiStateBinding<S> binding;
    private final PiDirtySet dirtySet = new PiDirtySet();

    public PiHostState(Class<S> stateType) {
        this.binding = PiSchemas.require(Objects.requireNonNull(stateType, "stateType"));
        this.state = binding.newState();
    }

    public S viewState() {
        return state;
    }

    public boolean updateState(Consumer<S> change) {
        CompoundTag before = binding.saveFull(state);
        change.accept(state);
        CompoundTag after = binding.saveFull(state);
        boolean changed = false;
        for (PiFieldDescriptor field : binding.fields()) {
            if (!sameTag(before.get(field.key().id()), after.get(field.key().id()))) {
                dirtySet.mark(field.key());
                changed = true;
            }
        }
        return changed;
    }

    public CompoundTag saveFull() {
        return binding.saveFull(state);
    }

    public CompoundTag saveFiltered(Predicate<PiFieldDescriptor> filter) {
        CompoundTag tag = binding.saveFull(state);
        pruneFields(tag, filter);
        return tag;
    }

    public void loadFull(CompoundTag tag, PiDecodeContext context) {
        binding.loadFull(state, tag, context);
        dirtySet.clear();
    }

    public CompoundTag saveClientView() {
        return binding.saveClientView(state);
    }

    public CompoundTag writeDelta() {
        return binding.writeDelta(state, dirtySet);
    }

    public CompoundTag writeDelta(Predicate<PiFieldDescriptor> filter) {
        PiDirtySet filtered = new PiDirtySet();
        for (PiFieldDescriptor field : binding.fields()) {
            if (filter.test(field) && dirtySet.contains(field.key())) {
                filtered.mark(field.key());
            }
        }
        return binding.writeDelta(state, filtered);
    }

    public void applyDelta(CompoundTag tag, PiDecodeContext context) {
        binding.applyDelta(state, tag, context);
    }

    public boolean hasDirty(Predicate<PiFieldDescriptor> filter) {
        for (PiFieldDescriptor field : binding.fields()) {
            if (filter.test(field) && dirtySet.contains(field.key())) {
                return true;
            }
        }
        return false;
    }

    public PiDirtySet dirtySet() {
        return dirtySet;
    }

    public void clearDirty() {
        dirtySet.clear();
    }

    public void clearDirty(Predicate<PiFieldDescriptor> filter) {
        for (PiFieldDescriptor field : binding.fields()) {
            if (filter.test(field)) {
                dirtySet.clear(field.key());
            }
        }
    }

    private static boolean sameTag(Tag left, Tag right) {
        if (left == null || right == null) {
            return left == right;
        }
        return left.equals(right);
    }

    private void pruneFields(CompoundTag tag, Predicate<PiFieldDescriptor> filter) {
        for (PiFieldDescriptor field : binding.fields()) {
            if (!filter.test(field)) {
                tag.remove(field.key().id());
            }
        }
    }
}
