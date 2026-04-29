package org.pickaid.pibrary.api.facet;

/**
 * Optional lifecycle hook for facets that need to react once attached to a living entity.
 */
public interface PiAttachedLivingFacet {
    /**
     * Called after the facet instance has been created and attached.
     */
    void onAttached();
}
