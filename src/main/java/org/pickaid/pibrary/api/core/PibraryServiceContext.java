package org.pickaid.pibrary.api.core;

/**
 * Scoped service registry that can create isolated child views.
 */
public interface PibraryServiceContext extends PibraryServiceRegistry {
    /**
     * Creates a child context that falls back to this context for missing services.
     *
     * @return a new child context
     */
    @Override
    PibraryServiceContext child();
}
