package org.pickaid.pibrary.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PiConfigSpecTest {
    @Test
    void buildsScopedSpecWithEntryMetadata() {
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry("max_energy", Codec.INT, 100, entry -> entry
                        .comment("Maximum stored energy.")
                        .alsoValidate(PiConfigValidators.intRange(1, 10_000)))
                .entry("starter_spell", Codec.STRING, "fireball", entry -> entry
                        .comment("Spell granted to a new player.")
                        .alsoValidate(PiConfigValidators.notBlank("starter spell must not be blank")))
                .build();

        assertEquals(id("gameplay"), spec.id());
        assertEquals(PiConfigScope.COMMON_BOOTSTRAP, spec.scope());
        assertEquals(2, spec.entries().size());
        assertEquals(id("gameplay/max_energy"), spec.entries().get(0).id());
        assertEquals(List.of("Maximum stored energy."), spec.entries().get(0).comments());
        assertTrue(spec.validateDefaults().isEmpty());
        assertTrue(spec.find(id("gameplay/starter_spell")).isPresent());
    }

    @Test
    void rejectsDuplicateEntryIds() {
        PiConfigEntry<Integer> first = new PiConfigEntry<>(id("gameplay/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 100);
        PiConfigEntry<Integer> second = new PiConfigEntry<>(id("gameplay/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 200);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> PiConfigSpec
                .builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry(first)
                .entry(second)
                .build());

        assertEquals("duplicate config entry id: example:gameplay/max_energy", exception.getMessage());
    }

    @Test
    void rejectsEntriesFromAnotherScope() {
        PiConfigEntry<Integer> clientEntry = new PiConfigEntry<>(id("client/particles"), Codec.INT, PiConfigScope.CLIENT_LOCAL, 8);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> PiConfigSpec
                .builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry(clientEntry)
                .build());

        assertEquals(
                "config entry example:client/particles has scope CLIENT_LOCAL but spec scope is COMMON_BOOTSTRAP",
                exception.getMessage()
        );
    }

    @Test
    void validatesDefaultsBeforeSpecEscapes() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> PiConfigSpec
                .builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry("max_energy", Codec.INT, 0, entry -> entry
                        .alsoValidate(PiConfigValidators.intRange(1, 10_000)))
                .build());

        assertEquals(
                "invalid config defaults: example:gameplay/max_energy: must be inside [1, 10000]",
                exception.getMessage()
        );
    }

    @Test
    void validatorsComposeAndKeepFirstFailure() {
        PiConfigEntry<String> entry = PiConfigEntry
                .builder(id("gameplay/starter_spell"), Codec.STRING, PiConfigScope.COMMON_BOOTSTRAP, "fireball")
                .alsoValidate(PiConfigValidators.notBlank("must not be blank"))
                .alsoValidate(value -> value.contains(":") ? java.util.Optional.empty() : java.util.Optional.of("must include namespace"))
                .build();

        assertEquals(List.of("must not be blank"), entry.validate(""));
        assertEquals(List.of("must include namespace"), entry.validate("fireball"));
        assertTrue(entry.validate("example:fireball").isEmpty());
    }

    @Test
    void valuesReadDefaultsAndValidatedOverridesThroughTypedEntries() {
        PiConfigEntry<Integer> maxEnergy = PiConfigEntry
                .builder(id("gameplay/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 100)
                .alsoValidate(PiConfigValidators.intRange(1, 10_000))
                .build();
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry(maxEnergy)
                .build();

        PiConfigValues defaults = PiConfigValues.defaults(spec);
        assertEquals(100, defaults.get(maxEnergy));
        assertTrue(defaults.overrides().isEmpty());

        PiConfigValues values = defaults.toBuilder().set(maxEnergy, 250).build();
        assertEquals(250, values.get(maxEnergy));
        assertTrue(values.hasOverride(maxEnergy));
        assertThrows(UnsupportedOperationException.class, () -> values.overrides().clear());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> values.toBuilder().set(maxEnergy, 0));
        assertEquals("invalid config value for example:gameplay/max_energy: must be inside [1, 10000]", exception.getMessage());
    }

    @Test
    void valuesRejectEntriesOutsideTheSpec() {
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry("max_energy", Codec.INT, 100)
                .build();
        PiConfigEntry<Integer> foreign = new PiConfigEntry<>(id("other/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 100);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PiConfigValues.defaults(spec).get(foreign));

        assertEquals("config entry does not belong to spec: example:other/max_energy", exception.getMessage());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("example", path);
    }
}
