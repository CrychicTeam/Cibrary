package org.pickaid.pibrary.runtime.presentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.presentation.PiPresentationContext;
import org.pickaid.pibrary.api.presentation.PiPresentationHost;
import org.pickaid.pibrary.api.presentation.PiPresentationScope;
import org.pickaid.pibrary.api.presentation.PiPresentations;

class PiPresentationCacheTest {
    @Test
    void cacheReusesResolvedValueUntilTypeIsInvalidated() {
        InvalidationHost host = new InvalidationHost();

        TestView first = PiPresentations.worldRender().resolve(host, TestView.class, 0.0F);
        TestView second = PiPresentations.worldRender().resolve(host, TestView.class, 0.5F);

        PiPresentations.invalidate(host, TestView.class);

        TestView third = PiPresentations.worldRender().resolve(host, TestView.class, 1.0F);

        assertEquals("view-1", first.value());
        assertEquals("view-1", second.value());
        assertEquals("view-2", third.value());
    }

    @Test
    void clientApplyInvalidatesOnlyProjectionsThatDeclaredThatPolicy() {
        InvalidationHost host = new InvalidationHost();

        TestView firstWorld = PiPresentations.worldRender().resolve(host, TestView.class, 0.0F);
        StableHud firstHud = PiPresentations.hud().resolve(host, StableHud.class, 0.0F);

        PiPresentations.invalidateClientApply(host);

        TestView secondWorld = PiPresentations.worldRender().resolve(host, TestView.class, 0.0F);
        StableHud secondHud = PiPresentations.hud().resolve(host, StableHud.class, 0.0F);

        assertEquals("view-1", firstWorld.value());
        assertEquals("view-2", secondWorld.value());
        assertSame(firstHud, secondHud);
    }

    @Test
    void surfaceInvalidateOnlyRefreshesThatSurface() {
        SharedSurfaceHost host = new SharedSurfaceHost();

        SharedProjection firstWorld = PiPresentations.worldRender().resolve(host, SharedProjection.class, 0.0F);
        SharedProjection firstHud = PiPresentations.hud().resolve(host, SharedProjection.class, 0.0F);

        PiPresentations.hud().invalidate(host);

        SharedProjection secondWorld = PiPresentations.worldRender().resolve(host, SharedProjection.class, 0.0F);
        SharedProjection secondHud = PiPresentations.hud().resolve(host, SharedProjection.class, 0.0F);

        assertSame(firstWorld, secondWorld);
        assertNotSame(firstHud, secondHud);
        assertEquals("hud-2", secondHud.value());
    }

    @Test
    void invalidateAllForcesAllSurfacesToRecompute() {
        SharedSurfaceHost host = new SharedSurfaceHost();

        SharedProjection firstWorld = PiPresentations.worldRender().resolve(host, SharedProjection.class, 0.0F);
        SharedProjection firstHud = PiPresentations.hud().resolve(host, SharedProjection.class, 0.0F);
        SharedProjection firstScreen = PiPresentations.screens().resolve(host, SharedProjection.class, 0.0F);

        PiPresentations.invalidateAll(host);

        SharedProjection secondWorld = PiPresentations.worldRender().resolve(host, SharedProjection.class, 0.0F);
        SharedProjection secondHud = PiPresentations.hud().resolve(host, SharedProjection.class, 0.0F);
        SharedProjection secondScreen = PiPresentations.screens().resolve(host, SharedProjection.class, 0.0F);

        assertNotSame(firstWorld, secondWorld);
        assertNotSame(firstHud, secondHud);
        assertNotSame(firstScreen, secondScreen);
    }

    @Test
    void typeInvalidateRefreshesThatTypeAcrossAllSurfaces() {
        SharedSurfaceHost host = new SharedSurfaceHost();

        SharedProjection firstWorld = PiPresentations.worldRender().resolve(host, SharedProjection.class, 0.0F);
        SharedProjection firstHud = PiPresentations.hud().resolve(host, SharedProjection.class, 0.0F);
        SharedProjection firstScreen = PiPresentations.screens().resolve(host, SharedProjection.class, 0.0F);

        PiPresentations.invalidate(host, SharedProjection.class);

        SharedProjection secondWorld = PiPresentations.worldRender().resolve(host, SharedProjection.class, 0.0F);
        SharedProjection secondHud = PiPresentations.hud().resolve(host, SharedProjection.class, 0.0F);
        SharedProjection secondScreen = PiPresentations.screens().resolve(host, SharedProjection.class, 0.0F);

        assertNotSame(firstWorld, secondWorld);
        assertNotSame(firstHud, secondHud);
        assertNotSame(firstScreen, secondScreen);
    }

    @Test
    void clientTickAndMenuDataOnlyInvalidateMarkedProjectionTypes() {
        PolicyHost host = new PolicyHost();

        TickView firstTick = PiPresentations.hud().resolve(host, TickView.class, 0.0F);
        TickStableView firstTickStable = PiPresentations.hud().resolve(host, TickStableView.class, 0.0F);
        MenuView firstMenu = PiPresentations.screens().resolve(host, MenuView.class, 0.0F);
        MenuStableView firstMenuStable = PiPresentations.screens().resolve(host, MenuStableView.class, 0.0F);

        PiPresentations.invalidateClientTick(host);
        PiPresentations.invalidateMenuData(host);

        TickView secondTick = PiPresentations.hud().resolve(host, TickView.class, 0.0F);
        TickStableView secondTickStable = PiPresentations.hud().resolve(host, TickStableView.class, 0.0F);
        MenuView secondMenu = PiPresentations.screens().resolve(host, MenuView.class, 0.0F);
        MenuStableView secondMenuStable = PiPresentations.screens().resolve(host, MenuStableView.class, 0.0F);

        assertNotSame(firstTick, secondTick);
        assertSame(firstTickStable, secondTickStable);
        assertNotSame(firstMenu, secondMenu);
        assertSame(firstMenuStable, secondMenuStable);
        assertEquals(2, secondTick.version());
        assertEquals(2, secondMenu.version());
    }

    @Test
    void metadataAccessAndPolicyBucketsUseFreshContributionData() {
        MetadataHost host = new MetadataHost();

        host.renderDistance = 24.0D;
        host.globalRenderer = false;
        assertEquals(24.0D, PiPresentations.worldRender().renderDistance(host));
        assertEquals(false, PiPresentations.worldRender().globalRenderer(host));

        host.renderDistance = 48.0D;
        host.globalRenderer = true;
        assertEquals(48.0D, PiPresentations.worldRender().renderDistance(host));
        assertEquals(true, PiPresentations.worldRender().globalRenderer(host));

        TestView first = PiPresentations.worldRender().resolve(host, TestView.class, 0.0F);
        PiPresentations.invalidateClientApply(host);
        TestView second = PiPresentations.worldRender().resolve(host, TestView.class, 0.0F);
        assertSame(first, second);

        host.refreshOnClientApply = true;
        PiPresentations.invalidateClientApply(host);
        TestView third = PiPresentations.worldRender().resolve(host, TestView.class, 0.0F);
        assertNotSame(second, third);
    }

    @Test
    void partialTickOnlyAffectsRecomputeAfterInvalidation() {
        PartialTickHost host = new PartialTickHost();

        PartialTickView first = PiPresentations.worldRender().resolve(host, PartialTickView.class, 0.10F);
        PartialTickView second = PiPresentations.worldRender().resolve(host, PartialTickView.class, 0.90F);

        assertSame(first, second);
        assertEquals(1, first.version());
        assertEquals(0.10F, first.partialTick());

        PiPresentations.invalidate(host, PartialTickView.class);

        PartialTickView third = PiPresentations.worldRender().resolve(host, PartialTickView.class, 0.90F);
        assertNotSame(second, third);
        assertEquals(2, third.version());
        assertEquals(0.90F, third.partialTick());
    }

    @Test
    void policyInvalidationIsNoOpWhenHostCacheDoesNotExist() {
        NoCachePolicyHost host = new NoCachePolicyHost();
        assertFalse(PiPresentationCaches.hasCache(host));

        PiPresentations.invalidateClientApply(host);
        PiPresentations.invalidateClientTick(host);
        PiPresentations.invalidateMenuData(host);

        assertEquals(0, host.contributionCalls);
        assertFalse(PiPresentationCaches.hasCache(host));
    }

    @Test
    void resolveMissingRegistrationThrows() {
        MissingHost host = new MissingHost();
        assertThrows(
                IllegalStateException.class,
                () -> PiPresentations.worldRender().resolve(host, TestView.class, 0.0F));
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

    private static final class InvalidationHost implements PiPresentationHost {
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

    private static final class SharedSurfaceHost implements PiPresentationHost {
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

    private static final class PolicyHost implements PiPresentationHost {
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

    private static final class MissingHost implements PiPresentationHost {
        @Override
        public void contributePresentation(PiPresentationContext context) {
            context.hud().snapshot(
                    StableHud.class,
                    PiPresentationScope.OWNER,
                    partialTick -> new StableHud(1)
            );
        }
    }

    private static final class MetadataHost implements PiPresentationHost {
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

    private static final class PartialTickHost implements PiPresentationHost {
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

    private static final class NoCachePolicyHost implements PiPresentationHost {
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
