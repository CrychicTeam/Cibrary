package org.pickaid.pibrary.dev.example;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import org.pickaid.pibrary.api.service.PiLevelService;
import org.pickaid.pibrary.api.service.PiLevelServiceContext;
import org.pickaid.pibrary.api.service.PiStateLevelService;

/**
 * Minimal sample level-scoped service backed by {@link CounterState}.
 */
@PiLevelService(namespace = "pibrary", path = "counter_level")
public final class CounterLevelService extends PiStateLevelService<CounterState> {
    /**
     * Creates the sample level service.
     *
     * @param context generated level service context
     */
    public CounterLevelService(PiLevelServiceContext context) {
        super(context);
    }

    /**
     * Resolves the sample level service for the given server level.
     *
     * @param level authoritative server level
     * @return resolved counter level service
     */
    public static CounterLevelService get(ServerLevel level) {
        return CounterLevelServices.COUNTER_LEVEL.get(level);
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
