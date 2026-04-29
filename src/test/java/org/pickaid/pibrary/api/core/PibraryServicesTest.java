package org.pickaid.pibrary.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PibraryServicesTest {
    private static final PibraryServiceKey<String> GREETING =
            new PibraryServiceKey<>(ResourceLocation.fromNamespaceAndPath("test", "greeting"), String.class);

    @Test
    void childContextOverridesWithoutPollutingParent() {
        PibraryServices root = PibraryServices.create();
        root.register(GREETING, "root");

        PibraryServices child = root.child();
        assertEquals("root", child.require(GREETING));

        child.register(GREETING, "child");
        assertEquals("child", child.require(GREETING));
        assertEquals("root", root.require(GREETING));

        child.register(GREETING, null);
        assertEquals("root", child.require(GREETING));
    }
}
