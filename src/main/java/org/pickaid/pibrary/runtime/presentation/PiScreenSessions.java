package org.pickaid.pibrary.runtime.presentation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.screen.PiScreenSessionFactory;
import org.pickaid.pibrary.runtime.core.PiWeakIdentityMap;

/**
 * Client-local screen session access keyed by local owner identity.
 */
public final class PiScreenSessions {
    private static final PiWeakIdentityMap<Map<Class<?>, Object>> SESSIONS = new PiWeakIdentityMap<>();

    private PiScreenSessions() {
    }

    /**
     * Resolves a registered screen session for one local owner.
     *
     * @param owner local screen or menu owner controlling session lifetime
     * @param source presentation source that registered the session contract
     * @param type session type
     * @param <S> session value type
     * @return stable local session object
     */
    @SuppressWarnings("unchecked")
    public static synchronized <S> S session(Object owner, PiPresentationSource source, Class<S> type) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(type, "type");
        Map<Class<?>, Object> sessions = SESSIONS.computeIfAbsent(owner, ignored -> new LinkedHashMap<>());
        Object existing = sessions.get(type);
        if (existing != null) {
            return (S) existing;
        }
        PiScreenSessionFactory<S> factory = PiPresentationResolver.screenSessionFactory(source, type);
        S created = Objects.requireNonNull(factory.create(), "sessionFactory produced null");
        sessions.put(type, created);
        return created;
    }
}
