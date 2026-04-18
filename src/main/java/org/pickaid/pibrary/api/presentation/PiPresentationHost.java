package org.pickaid.pibrary.api.presentation;

/**
 * Host entry point for contributing presentation definitions.
 */
public interface PiPresentationHost {
    /**
     * Contributes presentation contracts to the provided context.
     *
     * @param context mutable collector context
     */
    void contributePresentation(PiPresentationContext context);
}
