package org.pickaid.pibrary.runtime.sync;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.pickaid.piserializekit.api.schema.PiFieldDescriptor;
import org.pickaid.piserializekit.api.schema.PiFieldKey;
import org.pickaid.piserializekit.api.schema.PiSyncScope;

public final class PiSyncFlushPlan {
    private final Map<PiSyncScope, List<PiFieldKey>> keys = new EnumMap<>(PiSyncScope.class);

    public PiSyncFlushPlan add(PiFieldKey key, PiSyncScope scope) {
        keys.computeIfAbsent(scope, ignored -> new ArrayList<>()).add(key);
        return this;
    }

    public PiSyncFlushPlan add(PiFieldDescriptor descriptor) {
        return add(descriptor.key(), descriptor.syncScope());
    }

    public List<PiFieldKey> keys(PiSyncScope scope) {
        return List.copyOf(keys.getOrDefault(scope, List.of()));
    }
}
