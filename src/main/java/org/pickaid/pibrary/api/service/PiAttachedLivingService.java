package org.pickaid.pibrary.api.service;

/**
 * Optional lifecycle hook for services that need to react once attached to a living entity.
 */
public interface PiAttachedLivingService {
    /**
     * Called after the service instance has been created and attached.
     */
    void onAttached();
}
