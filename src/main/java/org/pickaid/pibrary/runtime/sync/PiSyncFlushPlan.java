package org.pickaid.pibrary.runtime.sync;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.pickaid.piserializekit.api.schema.PiFieldDescriptor;
import org.pickaid.piserializekit.api.schema.PiFieldKey;
import org.pickaid.piserializekit.api.schema.PiSyncScope;

/**
 * Groups generated field keys by sync scope for later flush planning.
 */
public final class PiSyncFlushPlan {
    private final Map<PiSyncScope, List<PiFieldKey>> keys = new EnumMap<>(PiSyncScope.class);

    /**
     * Adds a field key to the plan under the given scope.
     *
     * @param key field key
     * @param scope sync scope
     * @return this plan
     */
    public PiSyncFlushPlan add(PiFieldKey key, PiSyncScope scope) {
        keys.computeIfAbsent(scope, ignored -> new ArrayList<>()).add(key);
        return this;
    }

    /**
     * Adds a generated field descriptor to the plan.
     *
     * @param descriptor field descriptor
     * @return this plan
     */
    public PiSyncFlushPlan add(PiFieldDescriptor descriptor) {
        return add(descriptor.key(), descriptor.syncScope());
    }

    /**
     * Returns the keys scheduled for one sync scope.
     *
     * @param scope sync scope
     * @return immutable key list
     */
    public List<PiFieldKey> keys(PiSyncScope scope) {
        return List.copyOf(keys.getOrDefault(scope, List.of()));
    }
}
