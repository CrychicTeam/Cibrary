package org.pickaid.pibrary.runtime.presentation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.pickaid.pibrary.api.presentation.PiPresentationHost;
import org.pickaid.pibrary.api.presentation.screen.PiScreenSessionFactory;
import org.pickaid.pibrary.runtime.core.PiWeakIdentityStore;

/**
 * Client-local screen session store keyed by local owner identity.
 */
public final class PiScreenSessionStore {
    private static final PiWeakIdentityStore<Map<Class<?>, Object>> SESSIONS = new PiWeakIdentityStore<>();

    private PiScreenSessionStore() {
    }

    /**
     * Resolves a registered screen session for one local owner.
     *
     * @param owner local screen or menu owner controlling session lifetime
     * @param host presentation host that registered the session contract
     * @param type session type
     * @param <S> session value type
     * @return stable local session object
     */
    @SuppressWarnings("unchecked")
    public static synchronized <S> S session(Object owner, PiPresentationHost host, Class<S> type) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(type, "type");
        Map<Class<?>, Object> sessions = SESSIONS.computeIfAbsent(owner, ignored -> new LinkedHashMap<>());
        Object existing = sessions.get(type);
        if (existing != null) {
            return (S) existing;
        }
        PiScreenSessionFactory<S> factory = PiPresentationResolver.screenSessionFactory(host, type);
        S created = Objects.requireNonNull(factory.create(), "sessionFactory produced null");
        sessions.put(type, created);
        return created;
    }
}
