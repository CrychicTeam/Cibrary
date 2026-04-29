package org.pickaid.pibrary.api.presentation.screen;

import org.pickaid.pibrary.api.presentation.PiPresentationFactory;

/**
 * Screen projection contribution contract.
 */
public interface PiScreenPresentationContext {
    /**
     * Registers a screen projection snapshot.
     *
     * @param type projection type
     * @param factory projection factory
     * @param <V> projection value type
     */
    <V> void snapshot(Class<V> type, PiPresentationFactory<V> factory);

    /**
     * Registers a screen session model factory.
     *
     * @param type session type
     * @param factory session factory
     * @param <S> session value type
     */
    <S> void session(Class<S> type, PiScreenSessionFactory<S> factory);

    /**
     * Marks projection type for refresh after client apply.
     *
     * @param type projection type
     */
    void refreshOnClientApply(Class<?> type);

    /**
     * Marks projection type for refresh on menu data updates.
     *
     * @param type projection type
     */
    void refreshOnMenuData(Class<?> type);
}
