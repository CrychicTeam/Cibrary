package org.pickaid.pibrary.runtime.registrate;

public final class PiRegistrateEventBridge {
    private PiRegistrateEventBridge() {
    }

    public static void install() {
        // Phase 1 keeps this bridge thin. Existing runtime event hooks stay where they are.
    }
}
