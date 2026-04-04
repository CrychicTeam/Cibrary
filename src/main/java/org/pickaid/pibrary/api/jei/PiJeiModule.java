package org.pickaid.pibrary.api.jei;

@FunctionalInterface
public interface PiJeiModule {
    void contribute(PiJeiBridge bridge);
}
