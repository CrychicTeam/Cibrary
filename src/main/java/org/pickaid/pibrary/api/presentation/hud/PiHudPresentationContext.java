package org.pickaid.pibrary.api.presentation.hud;

import org.pickaid.pibrary.api.presentation.PiPresentationFactory;
import org.pickaid.pibrary.api.presentation.PiPresentationScope;

/**
 * HUD projection contribution contract.
 */
public interface PiHudPresentationContext {
    /**
     * Registers a HUD projection snapshot.
     *
     * @param type projection type
     * @param scope replication scope
     * @param factory projection factory
     * @param <V> projection value type
     */
    <V> void snapshot(Class<V> type, PiPresentationScope scope, PiPresentationFactory<V> factory);

    /**
     * Marks projection type for refresh after client apply.
     *
     * @param type projection type
     */
    void refreshOnClientApply(Class<?> type);

    /**
     * Marks projection type for refresh on client tick.
     *
     * @param type projection type
     */
    void refreshOnClientTick(Class<?> type);
}
