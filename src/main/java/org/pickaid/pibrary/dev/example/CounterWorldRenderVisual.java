package org.pickaid.pibrary.dev.example;

/**
 * Example world-render projection derived from {@link CounterState}.
 *
 * @param text displayed counter text
 * @param color packed ARGB text color
 */
public record CounterWorldRenderVisual(String text, int color) {
    /**
     * Creates a render projection from synced counter state.
     *
     * @param state backing synced state
     * @return world-render projection
     */
    public static CounterWorldRenderVisual from(CounterState state) {
        return new CounterWorldRenderVisual(Integer.toString(state.count), 0x80FF80);
    }
}
