package org.pickaid.pibrary.api.presentation;

/**
 * Source entry point for contributing presentation definitions.
 */
public interface PiPresentationSource {
    /**
     * Contributes presentation contracts to the provided context.
     *
     * @param context mutable collector context
     */
    void contributePresentation(PiPresentationContext context);
}
