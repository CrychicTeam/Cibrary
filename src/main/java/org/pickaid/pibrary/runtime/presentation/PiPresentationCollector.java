package org.pickaid.pibrary.runtime.presentation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.pickaid.pibrary.api.presentation.PiPresentationContext;
import org.pickaid.pibrary.api.presentation.PiPresentationFactory;
import org.pickaid.pibrary.api.presentation.PiPresentationKey;
import org.pickaid.pibrary.api.presentation.PiPresentationScope;
import org.pickaid.pibrary.api.presentation.PiPresentationSurface;
import org.pickaid.pibrary.api.presentation.hud.PiHudPresentationContext;
import org.pickaid.pibrary.api.presentation.screen.PiScreenPresentationContext;
import org.pickaid.pibrary.api.presentation.screen.PiScreenSessionFactory;
import org.pickaid.pibrary.api.presentation.world.PiWorldRenderPresentationContext;

/**
 * Mutable collector used by hosts to register presentation contracts.
 */
public final class PiPresentationCollector implements PiPresentationContext {
    private final EnumMap<PiPresentationSurface, List<PiPresentationRegistration<?>>> registrations =
            new EnumMap<>(PiPresentationSurface.class);
    private final EnumMap<PiPresentationSurface, Set<Class<?>>> clientApplyRefreshes =
            new EnumMap<>(PiPresentationSurface.class);
    private final EnumMap<PiPresentationSurface, Set<Class<?>>> clientTickRefreshes =
            new EnumMap<>(PiPresentationSurface.class);
    private final EnumMap<PiPresentationSurface, Set<Class<?>>> menuDataRefreshes =
            new EnumMap<>(PiPresentationSurface.class);
    private final Map<Class<?>, PiScreenSessionFactory<?>> screenSessions = new LinkedHashMap<>();
    private final EnumMap<PiPresentationSurface, Set<Class<?>>> registeredTypesBySurface =
            new EnumMap<>(PiPresentationSurface.class);
    private final PiWorldRenderPresentationContext worldContext = new WorldContext();
    private final PiHudPresentationContext hudContext = new HudContext();
    private final PiScreenPresentationContext screenContext = new ScreenContext();
    private double worldRenderDistance = 64.0D;
    private boolean globalRenderer;

    /**
     * Creates an empty collector with per-surface metadata buckets.
     */
    public PiPresentationCollector() {
        for (PiPresentationSurface surface : PiPresentationSurface.values()) {
            registrations.put(surface, new ArrayList<>());
            clientApplyRefreshes.put(surface, new LinkedHashSet<>());
            clientTickRefreshes.put(surface, new LinkedHashSet<>());
            menuDataRefreshes.put(surface, new LinkedHashSet<>());
            registeredTypesBySurface.put(surface, new LinkedHashSet<>());
        }
    }

    @Override
    public PiWorldRenderPresentationContext worldRender() {
        return worldContext;
    }

    @Override
    public PiHudPresentationContext hud() {
        return hudContext;
    }

    @Override
    public PiScreenPresentationContext screens() {
        return screenContext;
    }

    /**
     * Freezes all collected metadata into an immutable contribution.
     *
     * @return immutable contribution
     */
    public PiPresentationContribution freeze() {
        validateRefreshTargets(clientApplyRefreshes, "client apply");
        validateRefreshTargets(clientTickRefreshes, "client tick");
        validateRefreshTargets(menuDataRefreshes, "menu data");
        return new PiPresentationContribution(
                copyRegistrations(),
                copyRefreshes(clientApplyRefreshes),
                copyRefreshes(clientTickRefreshes),
                copyRefreshes(menuDataRefreshes),
                Map.copyOf(screenSessions),
                worldRenderDistance,
                globalRenderer
        );
    }

    private <V> void register(
            PiPresentationSurface surface,
            Class<V> type,
            PiPresentationScope scope,
            PiPresentationFactory<V> factory
    ) {
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(factory, "factory");
        PiPresentationKey<V> key = new PiPresentationKey<>(surface, type, scope);
        if (!registeredTypesBySurface.get(surface).add(type)) {
            throw new IllegalStateException("Duplicate presentation registration for " + type.getName()
                    + " on " + surface);
        }
        registrations.get(surface).add(new PiPresentationRegistration<>(key, factory));
    }

    private <S> void registerSession(Class<S> type, PiScreenSessionFactory<S> factory) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(factory, "factory");
        PiScreenSessionFactory<?> previous = screenSessions.putIfAbsent(type, factory);
        if (previous != null) {
            throw new IllegalStateException("Duplicate screen session factory for " + type.getName());
        }
    }

    private Map<PiPresentationSurface, List<PiPresentationRegistration<?>>> copyRegistrations() {
        EnumMap<PiPresentationSurface, List<PiPresentationRegistration<?>>> copy =
                new EnumMap<>(PiPresentationSurface.class);
        registrations.forEach((surface, values) -> copy.put(surface, List.copyOf(values)));
        return Map.copyOf(copy);
    }

    private Map<PiPresentationSurface, Set<Class<?>>> copyRefreshes(
            Map<PiPresentationSurface, Set<Class<?>>> source
    ) {
        EnumMap<PiPresentationSurface, Set<Class<?>>> copy =
                new EnumMap<>(PiPresentationSurface.class);
        source.forEach((surface, values) -> copy.put(surface, Set.copyOf(values)));
        return Map.copyOf(copy);
    }

    private void refreshOn(
            Map<PiPresentationSurface, Set<Class<?>>> target,
            PiPresentationSurface surface,
            Class<?> type
    ) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(surface, "surface");
        Objects.requireNonNull(type, "type");
        target.get(surface).add(type);
    }

    private void validateRefreshTargets(
            Map<PiPresentationSurface, Set<Class<?>>> refreshes,
            String refreshSource
    ) {
        for (PiPresentationSurface surface : PiPresentationSurface.values()) {
            Set<Class<?>> markedTypes = refreshes.get(surface);
            if (markedTypes == null || markedTypes.isEmpty()) {
                continue;
            }
            for (Class<?> markedType : markedTypes) {
                if (!isRegisteredOnSurface(surface, markedType)) {
                    throw new IllegalStateException(
                            "Refresh target " + markedType.getName()
                                    + " for " + refreshSource
                                    + " is not registered on " + surface);
                }
            }
        }
    }

    private boolean isRegisteredOnSurface(PiPresentationSurface surface, Class<?> type) {
        for (PiPresentationRegistration<?> registration : registrations.get(surface)) {
            if (registration.key().type().equals(type)) {
                return true;
            }
        }
        return false;
    }

    private final class WorldContext implements PiWorldRenderPresentationContext {
        @Override
        public <V> void snapshot(Class<V> type, PiPresentationScope scope, PiPresentationFactory<V> factory) {
            register(PiPresentationSurface.WORLD_RENDER, type, scope, factory);
        }

        @Override
        public void renderDistance(double blocks) {
            if (!Double.isFinite(blocks) || blocks < 0.0D) {
                throw new IllegalArgumentException("renderDistance must be finite and non-negative");
            }
            worldRenderDistance = blocks;
        }

        @Override
        public void globalRenderer(boolean value) {
            globalRenderer = value;
        }

        @Override
        public void refreshOnClientApply(Class<?> type) {
            refreshOn(clientApplyRefreshes, PiPresentationSurface.WORLD_RENDER, type);
        }
    }

    private final class HudContext implements PiHudPresentationContext {
        @Override
        public <V> void snapshot(Class<V> type, PiPresentationScope scope, PiPresentationFactory<V> factory) {
            register(PiPresentationSurface.HUD, type, scope, factory);
        }

        @Override
        public void refreshOnClientApply(Class<?> type) {
            refreshOn(clientApplyRefreshes, PiPresentationSurface.HUD, type);
        }

        @Override
        public void refreshOnClientTick(Class<?> type) {
            refreshOn(clientTickRefreshes, PiPresentationSurface.HUD, type);
        }
    }

    private final class ScreenContext implements PiScreenPresentationContext {
        @Override
        public <V> void snapshot(Class<V> type, PiPresentationFactory<V> factory) {
            register(PiPresentationSurface.SCREEN, type, PiPresentationScope.LOCAL_ONLY, factory);
        }

        @Override
        public <S> void session(Class<S> type, PiScreenSessionFactory<S> factory) {
            registerSession(type, factory);
        }

        @Override
        public void refreshOnClientApply(Class<?> type) {
            refreshOn(clientApplyRefreshes, PiPresentationSurface.SCREEN, type);
        }

        @Override
        public void refreshOnMenuData(Class<?> type) {
            refreshOn(menuDataRefreshes, PiPresentationSurface.SCREEN, type);
        }
    }
}
