package org.pickaid.pibrary.runtime.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.presentation.PiPresentationContext;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentationScope;
import org.pickaid.pibrary.api.presentation.PiPresentations;

class PiPresentationCacheTest {
    @Test
    void cacheReusesResolvedValueUntilTypeIsInvalidated() {
        InvalidationHost source = new InvalidationHost();

        TestView first = PiPresentations.worldRender().resolve(source, TestView.class, 0.0F);
        TestView second = PiPresentations.worldRender().resolve(source, TestView.class, 0.5F);

        PiPresentations.invalidate(source, TestView.class);

        TestView third = PiPresentations.worldRender().resolve(source, TestView.class, 1.0F);

        assertEquals("view-1", first.value());
        assertEquals("view-1", second.value());
        assertEquals("view-2", third.value());
    }

    @Test
    void clientApplyInvalidatesOnlyProjectionsThatDeclaredThatPolicy() {
        InvalidationHost source = new InvalidationHost();

        TestView firstWorld = PiPresentations.worldRender().resolve(source, TestView.class, 0.0F);
        StableHud firstHud = PiPresentations.hud().resolve(source, StableHud.class, 0.0F);

        PiPresentations.invalidateClientApply(source);

        TestView secondWorld = PiPresentations.worldRender().resolve(source, TestView.class, 0.0F);
        StableHud secondHud = PiPresentations.hud().resolve(source, StableHud.class, 0.0F);

        assertEquals("view-1", firstWorld.value());
        assertEquals("view-2", secondWorld.value());
        assertSame(firstHud, secondHud);
    }

    @Test
    void surfaceInvalidateOnlyRefreshesThatSurface() {
        SharedSurfaceHost source = new SharedSurfaceHost();

        SharedProjection firstWorld = PiPresentations.worldRender().resolve(source, SharedProjection.class, 0.0F);
        SharedProjection firstHud = PiPresentations.hud().resolve(source, SharedProjection.class, 0.0F);

        PiPresentations.hud().invalidate(source);

        SharedProjection secondWorld = PiPresentations.worldRender().resolve(source, SharedProjection.class, 0.0F);
        SharedProjection secondHud = PiPresentations.hud().resolve(source, SharedProjection.class, 0.0F);

        assertSame(firstWorld, secondWorld);
        assertNotSame(firstHud, secondHud);
        assertEquals("hud-2", secondHud.value());
    }

    @Test
    void invalidateAllForcesAllSurfacesToRecompute() {
        SharedSurfaceHost source = new SharedSurfaceHost();

        SharedProjection firstWorld = PiPresentations.worldRender().resolve(source, SharedProjection.class, 0.0F);
        SharedProjection firstHud = PiPresentations.hud().resolve(source, SharedProjection.class, 0.0F);
        SharedProjection firstScreen = PiPresentations.screens().resolve(source, SharedProjection.class, 0.0F);

        PiPresentations.invalidateAll(source);

        SharedProjection secondWorld = PiPresentations.worldRender().resolve(source, SharedProjection.class, 0.0F);
        SharedProjection secondHud = PiPresentations.hud().resolve(source, SharedProjection.class, 0.0F);
        SharedProjection secondScreen = PiPresentations.screens().resolve(source, SharedProjection.class, 0.0F);

        assertNotSame(firstWorld, secondWorld);
        assertNotSame(firstHud, secondHud);
        assertNotSame(firstScreen, secondScreen);
    }

    @Test
    void typeInvalidateRefreshesThatTypeAcrossAllSurfaces() {
        SharedSurfaceHost source = new SharedSurfaceHost();

        SharedProjection firstWorld = PiPresentations.worldRender().resolve(source, SharedProjection.class, 0.0F);
        SharedProjection firstHud = PiPresentations.hud().resolve(source, SharedProjection.class, 0.0F);
        SharedProjection firstScreen = PiPresentations.screens().resolve(source, SharedProjection.class, 0.0F);

        PiPresentations.invalidate(source, SharedProjection.class);

        SharedProjection secondWorld = PiPresentations.worldRender().resolve(source, SharedProjection.class, 0.0F);
        SharedProjection secondHud = PiPresentations.hud().resolve(source, SharedProjection.class, 0.0F);
        SharedProjection secondScreen = PiPresentations.screens().resolve(source, SharedProjection.class, 0.0F);

        assertNotSame(firstWorld, secondWorld);
        assertNotSame(firstHud, secondHud);
        assertNotSame(firstScreen, secondScreen);
    }

    @Test
    void clientTickAndMenuDataOnlyInvalidateMarkedProjectionTypes() {
        PolicyHost source = new PolicyHost();

        TickView firstTick = PiPresentations.hud().resolve(source, TickView.class, 0.0F);
        TickStableView firstTickStable = PiPresentations.hud().resolve(source, TickStableView.class, 0.0F);
        MenuView firstMenu = PiPresentations.screens().resolve(source, MenuView.class, 0.0F);
        MenuStableView firstMenuStable = PiPresentations.screens().resolve(source, MenuStableView.class, 0.0F);

        PiPresentations.invalidateClientTick(source);
        PiPresentations.invalidateMenuData(source);

        TickView secondTick = PiPresentations.hud().resolve(source, TickView.class, 0.0F);
        TickStableView secondTickStable = PiPresentations.hud().resolve(source, TickStableView.class, 0.0F);
        MenuView secondMenu = PiPresentations.screens().resolve(source, MenuView.class, 0.0F);
        MenuStableView secondMenuStable = PiPresentations.screens().resolve(source, MenuStableView.class, 0.0F);

        assertNotSame(firstTick, secondTick);
        assertSame(firstTickStable, secondTickStable);
        assertNotSame(firstMenu, secondMenu);
        assertSame(firstMenuStable, secondMenuStable);
        assertEquals(2, secondTick.version());
        assertEquals(2, secondMenu.version());
    }

    @Test
    void metadataAccessAndPolicyBucketsUseFreshContributionData() {
        MetadataHost source = new MetadataHost();

        source.renderDistance = 24.0D;
        source.globalRenderer = false;
        assertEquals(24.0D, PiPresentations.worldRender().renderDistance(source));
        assertEquals(false, PiPresentations.worldRender().globalRenderer(source));

        source.renderDistance = 48.0D;
        source.globalRenderer = true;
        assertEquals(48.0D, PiPresentations.worldRender().renderDistance(source));
        assertEquals(true, PiPresentations.worldRender().globalRenderer(source));

        TestView first = PiPresentations.worldRender().resolve(source, TestView.class, 0.0F);
        PiPresentations.invalidateClientApply(source);
        TestView second = PiPresentations.worldRender().resolve(source, TestView.class, 0.0F);
        assertSame(first, second);

        source.refreshOnClientApply = true;
        PiPresentations.invalidateClientApply(source);
        TestView third = PiPresentations.worldRender().resolve(source, TestView.class, 0.0F);
        assertNotSame(second, third);
    }

    @Test
    void partialTickOnlyAffectsRecomputeAfterInvalidation() {
        PartialTickHost source = new PartialTickHost();

        PartialTickView first = PiPresentations.worldRender().resolve(source, PartialTickView.class, 0.10F);
        PartialTickView second = PiPresentations.worldRender().resolve(source, PartialTickView.class, 0.90F);

        assertSame(first, second);
        assertEquals(1, first.version());
        assertEquals(0.10F, first.partialTick());

        PiPresentations.invalidate(source, PartialTickView.class);

        PartialTickView third = PiPresentations.worldRender().resolve(source, PartialTickView.class, 0.90F);
        assertNotSame(second, third);
        assertEquals(2, third.version());
        assertEquals(0.90F, third.partialTick());
    }

    @Test
    void policyInvalidationIsNoOpWhenHostCacheDoesNotExist() {
        NoCachePolicyHost source = new NoCachePolicyHost();
        assertFalse(PiPresentationCaches.hasCache(source));

        PiPresentations.invalidateClientApply(source);
        PiPresentations.invalidateClientTick(source);
        PiPresentations.invalidateMenuData(source);

        assertEquals(0, source.contributionCalls);
        assertFalse(PiPresentationCaches.hasCache(source));
    }

    @Test
    void resolveMissingRegistrationThrows() {
        MissingHost source = new MissingHost();
        assertThrows(
                IllegalStateException.class,
                () -> PiPresentations.worldRender().resolve(source, TestView.class, 0.0F));
    }

    record TestView(String value) {
    }

    record StableHud(int version) {
    }

    record SharedProjection(String value) {
    }

    record TickView(int version) {
    }

    record TickStableView(int version) {
    }

    record MenuView(int version) {
    }

    record MenuStableView(int version) {
    }

    record PartialTickView(int version, float partialTick) {
    }

    private static final class InvalidationHost implements PiPresentationSource {
        private int worldVersion;
        private int hudVersion;

        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.worldRender().snapshot(
                    TestView.class,
                    PiPresentationScope.TRACKING,
                    partialTick -> new TestView("view-" + ++worldVersion)
            );
            context.worldRender().refreshOnClientApply(TestView.class);

            context.hud().snapshot(
                    StableHud.class,
                    PiPresentationScope.OWNER,
                    partialTick -> new StableHud(++hudVersion)
            );
        }
    }

    private static final class SharedSurfaceHost implements PiPresentationSource {
        private int worldVersion;
        private int hudVersion;
        private int screenVersion;

        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.worldRender().snapshot(
                    SharedProjection.class,
                    PiPresentationScope.TRACKING,
                    partialTick -> new SharedProjection("world-" + ++worldVersion)
            );
            context.hud().snapshot(
                    SharedProjection.class,
                    PiPresentationScope.OWNER,
                    partialTick -> new SharedProjection("hud-" + ++hudVersion)
            );
            context.screens().snapshot(
                    SharedProjection.class,
                    partialTick -> new SharedProjection("screen-" + ++screenVersion)
            );
        }
    }

    private static final class PolicyHost implements PiPresentationSource {
        private int tickVersion;
        private int tickStableVersion;
        private int menuVersion;
        private int menuStableVersion;

        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.hud().snapshot(
                    TickView.class,
                    PiPresentationScope.OWNER,
                    partialTick -> new TickView(++tickVersion)
            );
            context.hud().snapshot(
                    TickStableView.class,
                    PiPresentationScope.OWNER,
                    partialTick -> new TickStableView(++tickStableVersion)
            );
            context.hud().refreshOnClientTick(TickView.class);

            context.screens().snapshot(
                    MenuView.class,
                    partialTick -> new MenuView(++menuVersion)
            );
            context.screens().snapshot(
                    MenuStableView.class,
                    partialTick -> new MenuStableView(++menuStableVersion)
            );
            context.screens().refreshOnMenuData(MenuView.class);
        }
    }

    private static final class MissingHost implements PiPresentationSource {
        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.hud().snapshot(
                    StableHud.class,
                    PiPresentationScope.OWNER,
                    partialTick -> new StableHud(1)
            );
        }
    }

    private static final class MetadataHost implements PiPresentationSource {
        private int version;
        private double renderDistance;
        private boolean globalRenderer;
        private boolean refreshOnClientApply;

        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.worldRender().snapshot(
                    TestView.class,
                    PiPresentationScope.TRACKING,
                    partialTick -> new TestView("meta-" + ++version)
            );
            context.worldRender().renderDistance(renderDistance);
            context.worldRender().globalRenderer(globalRenderer);
            if (refreshOnClientApply) {
                context.worldRender().refreshOnClientApply(TestView.class);
            }
        }
    }

    private static final class PartialTickHost implements PiPresentationSource {
        private int version;

        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.worldRender().snapshot(
                    PartialTickView.class,
                    PiPresentationScope.TRACKING,
                    partialTick -> new PartialTickView(++version, partialTick)
            );
        }
    }

    private static final class NoCachePolicyHost implements PiPresentationSource {
        private int contributionCalls;

        @Override
        public void contributePresentation(PiPresentationContext context) {
            contributionCalls++;
            context.worldRender().snapshot(
                    TestView.class,
                    PiPresentationScope.TRACKING,
                    partialTick -> new TestView("unused")
            );
            context.worldRender().refreshOnClientApply(TestView.class);
        }
    }
}
