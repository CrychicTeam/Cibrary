package org.pickaid.pibrary.dev.example;

/**
 * Example HUD projection derived from {@link CounterState}.
 *
 * @param energy owner-visible energy value
 * @param ownerName owner-facing name text
 */
public record CounterHudModel(int energy, String ownerName) {
    /**
     * Creates a HUD model from synced counter state.
     *
     * @param state backing synced state
     * @return HUD projection
     */
    public static CounterHudModel from(CounterState state) {
        return new CounterHudModel(state.energy, state.ownerName);
    }
}
