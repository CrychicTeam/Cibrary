package org.pickaid.pibrary.runtime.presentation;

import java.util.List;
import java.util.Objects;
import org.pickaid.pibrary.api.presentation.PiPresentationHost;
import org.pickaid.pibrary.api.presentation.PiPresentationSurface;
import org.pickaid.pibrary.api.presentation.screen.PiScreenSessionFactory;

/**
 * Resolves host projections by surface and projection type.
 */
public final class PiPresentationResolver {
    private PiPresentationResolver() {
    }

    /**
     * Collects presentation registrations and metadata from one host.
     *
     * @param host presentation host
     * @return frozen host contribution
     */
    public static PiPresentationContribution collect(PiPresentationHost host) {
        Objects.requireNonNull(host, "host");
        PiPresentationCollector collector = new PiPresentationCollector();
        host.contributePresentation(collector);
        return collector.freeze();
    }

    /**
     * Resolves a projection value from a specific surface/type pair.
     *
     * @param host presentation host
     * @param surface projection surface
     * @param type projection type
     * @param partialTick partial tick value for factory execution
     * @param <V> projection value type
     * @return resolved projection value
     */
    public static <V> V resolve(PiPresentationHost host, PiPresentationSurface surface, Class<V> type, float partialTick) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(type, "type");
        PiPresentationCache cache = PiPresentationCaches.cache(host);
        return cache.resolve(
                surface,
                type,
                partialTick,
                tick -> resolveFresh(host, surface, type, tick));
    }

    /**
     * Resolves one registered screen-session factory by type.
     *
     * @param host presentation host
     * @param type session type
     * @param <S> session value type
     * @return registered session factory
     */
    @SuppressWarnings("unchecked")
    public static <S> PiScreenSessionFactory<S> screenSessionFactory(PiPresentationHost host, Class<S> type) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(type, "type");
        PiScreenSessionFactory<?> factory = collect(host).screenSessions().get(type);
        if (factory == null) {
            throw new IllegalStateException("Missing screen session for " + type.getName());
        }
        return (PiScreenSessionFactory<S>) factory;
    }

    @SuppressWarnings("unchecked")
    private static <V> V resolveFresh(
            PiPresentationHost host,
            PiPresentationSurface surface,
            Class<V> type,
            float partialTick
    ) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(type, "type");
        PiPresentationContribution contribution = collect(host);
        List<PiPresentationRegistration<?>> registrations =
                Objects.requireNonNull(contribution.registrations().get(surface), "registrations");
        PiPresentationRegistration<V> match = null;
        for (PiPresentationRegistration<?> registration : registrations) {
            if (registration.key().type() == type) {
                if (match != null) {
                    throw new IllegalStateException("Ambiguous presentation for " + type.getName() + " on " + surface);
                }
                match = (PiPresentationRegistration<V>) registration;
            }
        }
        if (match == null) {
            throw new IllegalStateException("Missing presentation for " + type.getName() + " on " + surface);
        }
        return match.factory().create(partialTick);
    }
}
