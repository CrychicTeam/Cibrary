package org.pickaid.pibrary.api.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.runtime.presentation.PiPresentationCollector;
import org.pickaid.pibrary.runtime.presentation.PiPresentationContribution;
import org.pickaid.pibrary.runtime.presentation.PiPresentationRegistration;

class PiPresentationContractsTest {
    @Test
    void collectorSeparatesHostRegistrationsBySurface() {
        PiPresentationCollector collector = new PiPresentationCollector();

        collector.worldRender().snapshot(String.class, PiPresentationScope.TRACKING, partialTick -> "world");
        collector.hud().snapshot(Integer.class, PiPresentationScope.OWNER, partialTick -> 3);
        collector.screens().session(TestSession.class, TestSession::new);

        assertEquals(1, collector.freeze().registrations().get(PiPresentationSurface.WORLD_RENDER).size());
        assertEquals(1, collector.freeze().registrations().get(PiPresentationSurface.HUD).size());
        assertEquals(1, collector.freeze().screenSessions().size());
        assertSame(
                PiPresentationScope.TRACKING,
                collector.freeze().registrations().get(PiPresentationSurface.WORLD_RENDER).get(0).key().scope());
    }

    @Test
    void duplicateScreenSessionRegistrationThrows() {
        PiPresentationCollector collector = new PiPresentationCollector();
        collector.screens().session(TestSession.class, TestSession::new);

        assertThrows(
                IllegalStateException.class,
                () -> collector.screens().session(TestSession.class, TestSession::new));
    }

    @Test
    void duplicateSnapshotRegistrationForSameKeyThrows() {
        PiPresentationCollector collector = new PiPresentationCollector();
        collector.worldRender().snapshot(String.class, PiPresentationScope.TRACKING, partialTick -> "one");

        assertThrows(
                IllegalStateException.class,
                () -> collector.worldRender().snapshot(String.class, PiPresentationScope.TRACKING, partialTick -> "two"));
    }

    @Test
    void duplicateSnapshotRegistrationForSameSurfaceAndTypeThrowsEvenWhenScopeDiffers() {
        PiPresentationCollector collector = new PiPresentationCollector();
        collector.worldRender().snapshot(String.class, PiPresentationScope.TRACKING, partialTick -> "one");

        assertThrows(
                IllegalStateException.class,
                () -> collector.worldRender().snapshot(String.class, PiPresentationScope.OWNER, partialTick -> "two"));
    }

    @Test
    void screenSnapshotUsesLocalOnlyScope() {
        PiPresentationCollector collector = new PiPresentationCollector();
        collector.screens().snapshot(String.class, partialTick -> "screen");

        PiPresentationContribution contribution = collector.freeze();
        assertSame(
                PiPresentationScope.LOCAL_ONLY,
                contribution.registrations().get(PiPresentationSurface.SCREEN).get(0).key().scope());
    }

    @Test
    void refreshBucketsAreSurfaceScopedAndImmutable() {
        PiPresentationCollector collector = new PiPresentationCollector();
        collector.worldRender().snapshot(String.class, PiPresentationScope.TRACKING, partialTick -> "world");
        collector.hud().snapshot(Integer.class, PiPresentationScope.OWNER, partialTick -> 7);
        collector.screens().snapshot(Double.class, partialTick -> 2.0D);

        collector.worldRender().refreshOnClientApply(String.class);
        collector.hud().refreshOnClientTick(Integer.class);
        collector.screens().refreshOnMenuData(Double.class);

        PiPresentationContribution contribution = collector.freeze();
        assertEquals(
                Set.of(String.class),
                contribution.clientApplyRefreshes().get(PiPresentationSurface.WORLD_RENDER));
        assertEquals(
                Set.of(Integer.class),
                contribution.clientTickRefreshes().get(PiPresentationSurface.HUD));
        assertEquals(
                Set.of(Double.class),
                contribution.menuDataRefreshes().get(PiPresentationSurface.SCREEN));
        assertTrue(contribution.clientApplyRefreshes().get(PiPresentationSurface.HUD).isEmpty());

        assertThrows(
                UnsupportedOperationException.class,
                () -> contribution.clientApplyRefreshes().put(PiPresentationSurface.HUD, Set.of(Integer.class)));
        assertThrows(
                UnsupportedOperationException.class,
                () -> contribution.clientTickRefreshes().get(PiPresentationSurface.HUD).add(String.class));
    }

    @Test
    void worldRenderDistanceAndGlobalRendererSurviveFreeze() {
        PiPresentationCollector collector = new PiPresentationCollector();
        collector.worldRender().renderDistance(96.5D);
        collector.worldRender().globalRenderer(true);

        PiPresentationContribution contribution = collector.freeze();
        assertEquals(96.5D, contribution.worldRenderDistance());
        assertTrue(contribution.globalRenderer());
    }

    @Test
    void frozenContributionIsImmutableSnapshot() {
        PiPresentationCollector collector = new PiPresentationCollector();
        collector.worldRender().snapshot(String.class, PiPresentationScope.TRACKING, partialTick -> "a");

        PiPresentationContribution first = collector.freeze();
        collector.worldRender().snapshot(Integer.class, PiPresentationScope.OWNER, partialTick -> 1);
        PiPresentationContribution second = collector.freeze();

        assertEquals(1, first.registrations().get(PiPresentationSurface.WORLD_RENDER).size());
        assertEquals(2, second.registrations().get(PiPresentationSurface.WORLD_RENDER).size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> first.registrations().get(PiPresentationSurface.WORLD_RENDER).add(
                        registration(PiPresentationSurface.WORLD_RENDER, Long.class, PiPresentationScope.OWNER)));
    }

    @Test
    void invalidRenderDistanceIsRejected() {
        PiPresentationCollector collector = new PiPresentationCollector();
        assertThrows(IllegalArgumentException.class, () -> collector.worldRender().renderDistance(-0.1D));
        assertThrows(IllegalArgumentException.class, () -> collector.worldRender().renderDistance(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> collector.worldRender().renderDistance(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> collector.worldRender().renderDistance(Double.NEGATIVE_INFINITY));
    }

    @Test
    void refreshTargetWithoutRegistrationFailsDuringFreeze() {
        PiPresentationCollector collector = new PiPresentationCollector();
        collector.worldRender().refreshOnClientApply(String.class);

        assertThrows(IllegalStateException.class, collector::freeze);
    }

    @Test
    void contributionConstructorDefensivelyCopiesNestedStructures() {
        EnumMap<PiPresentationSurface, List<PiPresentationRegistration<?>>> registrations =
                new EnumMap<>(PiPresentationSurface.class);
        List<PiPresentationRegistration<?>> worldRegistrations = new ArrayList<>();
        worldRegistrations.add(registration(PiPresentationSurface.WORLD_RENDER, String.class, PiPresentationScope.TRACKING));
        registrations.put(PiPresentationSurface.WORLD_RENDER, worldRegistrations);

        EnumMap<PiPresentationSurface, Set<Class<?>>> applyRefreshes = new EnumMap<>(PiPresentationSurface.class);
        Set<Class<?>> worldApply = new LinkedHashSet<>();
        worldApply.add(String.class);
        applyRefreshes.put(PiPresentationSurface.WORLD_RENDER, worldApply);

        EnumMap<PiPresentationSurface, Set<Class<?>>> tickRefreshes = new EnumMap<>(PiPresentationSurface.class);
        tickRefreshes.put(PiPresentationSurface.HUD, new LinkedHashSet<>(Set.of(Integer.class)));

        EnumMap<PiPresentationSurface, Set<Class<?>>> menuRefreshes = new EnumMap<>(PiPresentationSurface.class);
        menuRefreshes.put(PiPresentationSurface.SCREEN, new LinkedHashSet<>(Set.of(Double.class)));

        Map<Class<?>, org.pickaid.pibrary.api.presentation.screen.PiScreenSessionFactory<?>> sessions =
                new LinkedHashMap<>();
        sessions.put(TestSession.class, TestSession::new);

        PiPresentationContribution contribution = new PiPresentationContribution(
                registrations,
                applyRefreshes,
                tickRefreshes,
                menuRefreshes,
                sessions,
                64.0D,
                false
        );

        worldRegistrations.clear();
        worldApply.clear();
        sessions.clear();

        assertEquals(1, contribution.registrations().get(PiPresentationSurface.WORLD_RENDER).size());
        assertEquals(1, contribution.clientApplyRefreshes().get(PiPresentationSurface.WORLD_RENDER).size());
        assertEquals(1, contribution.screenSessions().size());
        assertThrows(
                UnsupportedOperationException.class,
                () -> contribution.registrations().put(PiPresentationSurface.HUD, List.of()));
        assertThrows(
                UnsupportedOperationException.class,
                () -> contribution.registrations().get(PiPresentationSurface.WORLD_RENDER).add(
                        registration(PiPresentationSurface.WORLD_RENDER, Integer.class, PiPresentationScope.OWNER)));
        assertThrows(
                UnsupportedOperationException.class,
                () -> contribution.screenSessions().put(Integer.class, () -> 1));
    }

    private static <V> PiPresentationRegistration<V> registration(
            PiPresentationSurface surface,
            Class<V> type,
            PiPresentationScope scope
    ) {
        return new PiPresentationRegistration<>(new PiPresentationKey<>(surface, type, scope), partialTick -> null);
    }

    private static final class TestSession {
    }
}
