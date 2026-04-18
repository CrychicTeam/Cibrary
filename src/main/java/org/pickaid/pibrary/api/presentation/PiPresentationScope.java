package org.pickaid.pibrary.api.presentation;

/**
 * Audience scope for a projected presentation value.
 */
public enum PiPresentationScope {
    /**
     * Visible only to the owning client.
     */
    OWNER,

    /**
     * Visible only to tracking clients.
     */
    TRACKING,

    /**
     * Visible to both the owner and tracking clients.
     */
    OWNER_AND_TRACKING,

    /**
     * Visible only inside local client runtime such as screen session flows.
     */
    LOCAL_ONLY
}
