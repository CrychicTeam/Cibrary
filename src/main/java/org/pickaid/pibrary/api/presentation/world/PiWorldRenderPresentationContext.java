package org.pickaid.pibrary.api.presentation.world;

import org.pickaid.pibrary.api.presentation.PiPresentationFactory;
import org.pickaid.pibrary.api.presentation.PiPresentationScope;

/**
 * World-render projection contribution contract.
 */
public interface PiWorldRenderPresentationContext {
    /**
     * Registers a world-render projection snapshot.
     *
     * @param type projection type
     * @param scope replication scope
     * @param factory projection factory
     * @param <V> projection value type
     */
    <V> void snapshot(Class<V> type, PiPresentationScope scope, PiPresentationFactory<V> factory);

    /**
     * Sets world-render projection distance in blocks.
     *
     * @param blocks world-render distance
     */
    void renderDistance(double blocks);

    /**
     * Declares whether a global renderer is required.
     *
     * @param value global renderer flag
     */
    void globalRenderer(boolean value);

    /**
     * Marks projection type for refresh after client apply.
     *
     * @param type projection type
     */
    void refreshOnClientApply(Class<?> type);
}
