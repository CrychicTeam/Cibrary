package org.pickaid.pibrary.dev.example;

import net.minecraft.world.entity.player.Player;
import org.pickaid.pibrary.api.presentation.PiPresentationContext;
import org.pickaid.pibrary.api.presentation.PiPresentationSource;
import org.pickaid.pibrary.api.presentation.PiPresentationScope;
import org.pickaid.pibrary.api.facet.PiAttachedLivingFacet;
import org.pickaid.pibrary.api.facet.PiCloneAwareLivingFacet;
import org.pickaid.pibrary.api.facet.PiLivingFacet;
import org.pickaid.pibrary.api.facet.PiLivingFacetContext;
import org.pickaid.pibrary.api.facet.PiStatePlayerFacet;

/**
 * Sample generated player facet showing state mutation, attach hooks, and clone hooks.
 */
@PiLivingFacet(namespace = "pibrary", path = "counter_player")
public final class CounterPlayerFacet extends PiStatePlayerFacet<CounterState>
        implements PiAttachedLivingFacet, PiCloneAwareLivingFacet, PiPresentationSource {
    /**
     * Creates the sample player facet.
     *
     * @param context generated living facet context
     */
    public CounterPlayerFacet(PiLivingFacetContext context) {
        super(context);
    }

    /**
     * Resolves the sample facet from a player.
     *
     * @param player source player
     * @return attached counter facet
     */
    public static CounterPlayerFacet get(Player player) {
        return CounterFacets.COUNTER_PLAYER.get(player);
    }

    /**
     * Increments the sample count.
     */
    public void increment() {
        updateState(state -> state.count++);
    }

    /**
     * Adds one point of energy.
     */
    public void incrementEnergy() {
        gainEnergy(1);
    }

    /**
     * Adds the given amount of energy.
     *
     * @param value energy to add
     */
    public void gainEnergy(int value) {
        updateState(state -> state.energy += value);
    }

    /**
     * Sets the sample client-only glow state.
     *
     * @param value transient glow value
     */
    public void setSessionGlow(int value) {
        updateState(state -> state.sessionGlow = value);
    }

    /**
     * Returns the current energy value.
     *
     * @return current energy
     */
    public int energy() {
        return viewState().energy;
    }

    /**
     * Returns the current client-only glow state.
     *
     * @return transient glow value
     */
    public int sessionGlow() {
        return viewState().sessionGlow;
    }

    /**
     * Returns the current count value.
     *
     * @return current count
     */
    public int getCount() {
        return viewState().count;
    }

    @Override
    public void contributePresentation(PiPresentationContext context) {
        context.hud().snapshot(
                CounterHudModel.class,
                PiPresentationScope.OWNER,
                partialTick -> CounterHudModel.from(viewState())
        );
        context.hud().refreshOnClientApply(CounterHudModel.class);
    }

    /**
     * Returns the number of currently dirty fields.
     *
     * @return dirty field count
     */
    public int dirtyCount() {
        return dirtySet().keys().size();
    }

    /**
     * Seeds the owner name once on the authoritative server side.
     */
    @Override
    public void onAttached() {
        Player player = player();
        if (player == null || player.level().isClientSide()) {
            return;
        }
        if ("fallback".equals(viewState().ownerName)) {
            updateState(state -> state.ownerName = player.getScoreboardName());
        }
    }

    /**
     * Restores the active flag after death-based player cloning.
     *
     * @param original original entity
     * @param wasDeath whether the clone came from death
     */
    @Override
    public void onCloned(net.minecraft.world.entity.LivingEntity original, boolean wasDeath) {
        if (!wasDeath) {
            return;
        }
        updateState(state -> state.active = true);
    }
}
