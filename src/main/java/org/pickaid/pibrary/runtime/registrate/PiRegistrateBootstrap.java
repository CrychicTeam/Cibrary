package org.pickaid.pibrary.runtime.registrate;

public final class PiRegistrateBootstrap {
    private static volatile boolean bootstrapped;

    private PiRegistrateBootstrap() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        PiRegistrateEventBridge.install();
        bootstrapped = true;
    }

    public static boolean isBootstrapped() {
        return bootstrapped;
    }
}
