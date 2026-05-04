package org.pickaid.pibrary.api.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PibraryScopesTest {
    private static final PibraryScopeKey<String> GREETING =
            new PibraryScopeKey<>(new ResourceLocation("test", "greeting"), String.class);

    @Test
    void childContextOverridesWithoutPollutingParent() {
        PibraryScope root = PibraryScopes.create();
        root.register(GREETING, "root");

        PibraryScope child = root.child();
        assertEquals("root", child.require(GREETING));

        child.register(GREETING, "child");
        assertEquals("child", child.require(GREETING));
        assertEquals("root", root.require(GREETING));

        child.register(GREETING, null);
        assertEquals("root", child.require(GREETING));
    }
}
