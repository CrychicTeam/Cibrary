package org.pickaid.pibrary.api.signal;

public record PiSignalBudget(
        int maxSignalsPerTick,
        int maxDepth,
        int sameActorThrottleTicks,
        int sameTypeThrottleTicks
) {
    public static PiSignalBudget defaults() {
        return new PiSignalBudget(256, 16, 0, 0);
    }
}
