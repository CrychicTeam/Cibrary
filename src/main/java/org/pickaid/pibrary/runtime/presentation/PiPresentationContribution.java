package org.pickaid.pibrary.runtime.presentation;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.pickaid.pibrary.api.presentation.PiPresentationSurface;
import org.pickaid.pibrary.api.presentation.screen.PiScreenSessionFactory;

/**
 * Immutable set of collected presentation metadata.
 *
 * @param registrations registrations keyed by surface
 * @param clientApplyRefreshes client-apply invalidation types keyed by surface
 * @param clientTickRefreshes client-tick invalidation types keyed by surface
 * @param menuDataRefreshes menu-data invalidation types keyed by surface
 * @param screenSessions screen session factories keyed by session type
 * @param worldRenderDistance world-render distance in blocks
 * @param globalRenderer global renderer flag
 */
public record PiPresentationContribution(
        Map<PiPresentationSurface, List<PiPresentationRegistration<?>>> registrations,
        Map<PiPresentationSurface, Set<Class<?>>> clientApplyRefreshes,
        Map<PiPresentationSurface, Set<Class<?>>> clientTickRefreshes,
        Map<PiPresentationSurface, Set<Class<?>>> menuDataRefreshes,
        Map<Class<?>, PiScreenSessionFactory<?>> screenSessions,
        double worldRenderDistance,
        boolean globalRenderer
) {
    public PiPresentationContribution {
        registrations = copyRegistrationMap(registrations, "registrations");
        clientApplyRefreshes = copyRefreshMap(clientApplyRefreshes, "clientApplyRefreshes");
        clientTickRefreshes = copyRefreshMap(clientTickRefreshes, "clientTickRefreshes");
        menuDataRefreshes = copyRefreshMap(menuDataRefreshes, "menuDataRefreshes");
        screenSessions = copyScreenSessions(screenSessions);
    }

    private static Map<PiPresentationSurface, List<PiPresentationRegistration<?>>> copyRegistrationMap(
            Map<PiPresentationSurface, List<PiPresentationRegistration<?>>> source,
            String name
    ) {
        Objects.requireNonNull(source, name);
        EnumMap<PiPresentationSurface, List<PiPresentationRegistration<?>>> copy =
                new EnumMap<>(PiPresentationSurface.class);
        for (Map.Entry<PiPresentationSurface, List<PiPresentationRegistration<?>>> entry : source.entrySet()) {
            PiPresentationSurface surface = Objects.requireNonNull(entry.getKey(), name + " contains null surface");
            List<PiPresentationRegistration<?>> values =
                    Objects.requireNonNull(entry.getValue(), name + " contains null registration list");
            copy.put(surface, List.copyOf(values));
        }
        return Map.copyOf(copy);
    }

    private static Map<PiPresentationSurface, Set<Class<?>>> copyRefreshMap(
            Map<PiPresentationSurface, Set<Class<?>>> source,
            String name
    ) {
        Objects.requireNonNull(source, name);
        EnumMap<PiPresentationSurface, Set<Class<?>>> copy = new EnumMap<>(PiPresentationSurface.class);
        for (Map.Entry<PiPresentationSurface, Set<Class<?>>> entry : source.entrySet()) {
            PiPresentationSurface surface = Objects.requireNonNull(entry.getKey(), name + " contains null surface");
            Set<Class<?>> values = Objects.requireNonNull(entry.getValue(), name + " contains null refresh set");
            copy.put(surface, Set.copyOf(values));
        }
        return Map.copyOf(copy);
    }

    private static Map<Class<?>, PiScreenSessionFactory<?>> copyScreenSessions(
            Map<Class<?>, PiScreenSessionFactory<?>> source
    ) {
        Objects.requireNonNull(source, "screenSessions");
        Map<Class<?>, PiScreenSessionFactory<?>> copy = new LinkedHashMap<>();
        for (Map.Entry<Class<?>, PiScreenSessionFactory<?>> entry : source.entrySet()) {
            Class<?> type = Objects.requireNonNull(entry.getKey(), "screenSessions contains null type");
            PiScreenSessionFactory<?> factory =
                    Objects.requireNonNull(entry.getValue(), "screenSessions contains null factory");
            copy.put(type, factory);
        }
        return Map.copyOf(copy);
    }
}
