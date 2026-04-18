package org.pickaid.pibrary.dev.example;

/**
 * Example screen projection derived from {@link CounterState}.
 *
 * @param title screen title
 * @param count synced counter value
 * @param energy synced energy value
 */
public record CounterScreenModel(String title, int count, int energy) {
    /**
     * Creates a screen model from synced counter state.
     *
     * @param state backing synced state
     * @return screen projection
     */
    public static CounterScreenModel from(CounterState state) {
        return new CounterScreenModel("Counter", state.count, state.energy);
    }
}
