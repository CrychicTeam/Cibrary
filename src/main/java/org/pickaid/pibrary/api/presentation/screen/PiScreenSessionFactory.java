package org.pickaid.pibrary.api.presentation.screen;

/**
 * Creates a screen session object.
 *
 * @param <S> session type
 */
@FunctionalInterface
public interface PiScreenSessionFactory<S> {
    /**
     * Creates a session instance.
     *
     * @return session instance
     */
    S create();
}
