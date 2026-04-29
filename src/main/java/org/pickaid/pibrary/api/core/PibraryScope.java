package org.pickaid.pibrary.api.core;

/**
 * Scoped value registry that can create isolated child views.
 */
public interface PibraryScope extends PibraryScopeRegistry {
    /**
     * Creates a child scope that falls back to this scope for missing values.
     *
     * @return a new child context
     */
    @Override
    PibraryScope child();
}
