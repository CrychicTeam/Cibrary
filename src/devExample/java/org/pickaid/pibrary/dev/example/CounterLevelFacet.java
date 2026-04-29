package org.pickaid.pibrary.dev.example;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.pickaid.pibrary.api.facet.PiLevelFacet;
import org.pickaid.pibrary.api.facet.PiLevelFacetContext;
import org.pickaid.pibrary.api.facet.PiStateLevelFacet;

/**
 * Minimal sample level-scoped facet backed by {@link CounterState}.
 */
@PiLevelFacet(namespace = "pibrary", path = "counter_level")
public final class CounterLevelFacet extends PiStateLevelFacet<CounterState> {
    /**
     * Creates the sample level facet.
     *
     * @param context generated level facet context
     */
    public CounterLevelFacet(PiLevelFacetContext context) {
        super(context);
    }

    /**
     * Resolves the sample level facet for the given server level.
     *
     * @param level authoritative server level
     * @return resolved counter level facet
     */
    public static CounterLevelFacet get(ServerLevel level) {
        return CounterLevelFacets.COUNTER_LEVEL.get(level);
    }

    /**
     * Increments the sample count.
     */
    public void increment() {
        updateState(state -> state.count++);
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
     * Updates the sample trial id.
     *
     * @param id new trial id
     */
    public void startTrial(ResourceLocation id) {
        updateState(state -> state.trial = id);
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
     * Returns the current count value.
     *
     * @return current count
     */
    public int count() {
        return viewState().count;
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
     * Returns the current transient glow value.
     *
     * @return transient glow value
     */
    public int sessionGlow() {
        return viewState().sessionGlow;
    }

    /**
     * Returns the current trial id.
     *
     * @return current trial id
     */
    public ResourceLocation trial() {
        return viewState().trial;
    }
}
