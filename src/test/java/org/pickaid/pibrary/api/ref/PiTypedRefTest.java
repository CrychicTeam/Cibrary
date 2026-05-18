package org.pickaid.pibrary.api.ref;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class PiTypedRefTest {
    @Test
    void typedRefCarriesIdAndValueType() {
        PiTypedRef<TestType> ref = PiTypedRef.of(new ResourceLocation("test", "active_skill"), TestType.class);

        assertEquals(new ResourceLocation("test", "active_skill"), ref.id());
        assertEquals(TestType.class, ref.valueType());
    }

    private static final class TestType {
    }
}
