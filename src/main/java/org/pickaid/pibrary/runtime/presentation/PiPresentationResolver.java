package org.pickaid.pibrary.runtime.presentation;

import java.util.List;
import java.util.Objects;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentationSurface;
import org.pickaid.pibrary.api.presentation.screen.PiScreenSessionFactory;

/**
 * Resolves source projections by surface and projection type.
 */
public final class PiPresentationResolver {
    private PiPresentationResolver() {
    }

    /**
     * Collects presentation registrations and metadata from one source.
     *
     * @param source presentation source
     * @return frozen source contribution
     */
    public static PiPresentationContribution collect(PiPresentationSource source) {
        Objects.requireNonNull(source, "source");
        PiPresentationCollector collector = new PiPresentationCollector();
        source.contributePresentation(collector);
        return collector.freeze();
    }

    /**
     * Resolves a projection value from a specific surface/type pair.
     *
     * @param source presentation source
     * @param surface projection surface
     * @param type projection type
     * @param partialTick partial tick value for factory execution
     * @param <V> projection value type
     * @return resolved projection value
     */
    public static <V> V resolve(PiPresentationSource source, PiPresentationSurface surface, Class<V> type, float partialTick) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(type, "type");
        PiPresentationCache cache = PiPresentationCaches.cache(source);
        return cache.resolve(
                surface,
                type,
                partialTick,
                tick -> resolveFresh(source, surface, type, tick));
    }

    /**
     * Resolves one registered screen-session factory by type.
     *
     * @param source presentation source
     * @param type session type
     * @param <S> session value type
     * @return registered session factory
     */
    @SuppressWarnings("unchecked")
    public static <S> PiScreenSessionFactory<S> screenSessionFactory(PiPresentationSource source, Class<S> type) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(type, "type");
        PiScreenSessionFactory<?> factory = collect(source).screenSessions().get(type);
        if (factory == null) {
            throw new IllegalStateException("Missing screen session for " + type.getName());
        }
        return (PiScreenSessionFactory<S>) factory;
    }

    @SuppressWarnings("unchecked")
    private static <V> V resolveFresh(
            PiPresentationSource source,
            PiPresentationSurface surface,
            Class<V> type,
            float partialTick
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(type, "type");
        PiPresentationContribution contribution = collect(source);
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
