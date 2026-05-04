package org.pickaid.pibrary.runtime.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.config.PiDataConfigType;
import org.pickaid.pibrary.api.config.PiConfigEntry;
import org.pickaid.pibrary.api.config.PiConfigScope;
import org.pickaid.pibrary.api.config.PiConfigSpec;
import org.pickaid.pibrary.api.config.PiConfigValidators;

class PiConfigResourceReloaderTest {
    @Test
    void loadsRegisteredSpecFromPreparedResourceMap() {
        PiConfigEntry<Integer> maxEnergy = PiConfigEntry
                .builder(id("gameplay/max_energy"), Codec.INT, PiConfigScope.SERVER_DATA_PACK, 100)
                .alsoValidate(PiConfigValidators.intRange(1, 10_000))
                .build();
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.SERVER_DATA_PACK)
                .entry(maxEnergy)
                .build();
        PiConfigResourceReloader reloader = new PiConfigResourceReloader("pi_config");
        PiConfigResourceBinding binding = reloader.register(spec);
        JsonObject json = new JsonObject();
        json.addProperty("max_energy", 250);

        reloader.apply(Map.of(id("gameplay"), json), null, null);

        assertEquals("pi_config", reloader.directory());
        assertEquals(250, binding.get(maxEnergy));
    }

    @Test
    void missingResourceFallsBackToDefaultsOnReload() {
        PiConfigEntry<Integer> maxEnergy = PiConfigEntry
                .builder(id("gameplay/max_energy"), Codec.INT, PiConfigScope.SERVER_DATA_PACK, 100)
                .build();
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.SERVER_DATA_PACK)
                .entry(maxEnergy)
                .build();
        PiConfigResourceReloader reloader = new PiConfigResourceReloader("pi_config");
        PiConfigResourceBinding binding = reloader.register(spec);
        JsonObject json = new JsonObject();
        json.addProperty("max_energy", 250);
        reloader.apply(Map.of(id("gameplay"), json), null, null);

        reloader.apply(Map.of(), null, null);

        assertEquals(100, binding.get(maxEnergy));
    }

    @Test
    void rejectsForgeScopedSpecsForDatapackLoader() {
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry("max_energy", Codec.INT, 100)
                .build();
        PiConfigResourceReloader reloader = new PiConfigResourceReloader("pi_config");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reloader.register(spec));

        assertEquals("datapack config spec must use SERVER_DATA_PACK scope: example:gameplay", exception.getMessage());
    }

    @Test
    void rejectsNonObjectResources() {
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.SERVER_DATA_PACK)
                .entry("max_energy", Codec.INT, 100)
                .build();
        PiConfigResourceReloader reloader = new PiConfigResourceReloader("pi_config");
        reloader.register(spec);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reloader.apply(Map.of(id("gameplay"), new com.google.gson.JsonPrimitive(true)), null, null));

        assertEquals("datapack config example:gameplay must be a JSON object", exception.getMessage());
    }

    @Test
    void loadsTypedDatapackConfigFamiliesFromSubdirectories() {
        PiDataConfigType<SpellConfig> spells = PiDataConfigType.merged(
                "spell",
                spellCodec(),
                values -> values.stream().reduce(new SpellConfig(0, 0), SpellConfig::plus));
        PiConfigResourceReloader reloader = new PiConfigResourceReloader("pi_config");
        PiDataConfigBinding<SpellConfig> binding = reloader.register(spells);
        JsonObject fireball = new JsonObject();
        fireball.addProperty("mana", 8);
        fireball.addProperty("cooldown", 120);
        JsonObject frostbolt = new JsonObject();
        frostbolt.addProperty("mana", 10);
        frostbolt.addProperty("cooldown", 80);

        reloader.apply(Map.of(
                id("spell/fireball"), fireball,
                id("spell/frostbolt"), frostbolt,
                id("unknown/ignored"), frostbolt
        ), null, null);

        assertEquals(new SpellConfig(8, 120), binding.requireEntry(id("fireball")));
        assertEquals(new SpellConfig(18, 200), binding.getMerged());
        assertEquals(2, binding.getAll().size());
        assertTrue(binding.find(id("missing")).isEmpty());
    }

    @Test
    void reloadWithoutTypedResourcesClearsDatapackConfigFamily() {
        PiDataConfigType<SpellConfig> spells = PiDataConfigType.create(
                "spell",
                spellCodec());
        PiConfigResourceReloader reloader = new PiConfigResourceReloader("pi_config");
        PiDataConfigBinding<SpellConfig> binding = reloader.register(spells);
        JsonObject fireball = new JsonObject();
        fireball.addProperty("mana", 8);
        fireball.addProperty("cooldown", 120);
        reloader.apply(Map.of(id("spell/fireball"), fireball), null, null);

        reloader.apply(Map.of(), null, null);

        assertEquals(0, binding.getAll().size());
    }

    @Test
    void derivedDatapackConfigViewsRebuildAfterReload() {
        PiDataConfigType<SpellConfig> spells = PiDataConfigType.create("spell", spellCodec());
        PiConfigResourceReloader reloader = new PiConfigResourceReloader("pi_config");
        PiDataConfigBinding<SpellConfig> binding = reloader.register(spells);
        PiDataConfigView<SpellConfig, Map<Integer, SpellConfig>> byMana = binding.view(values -> {
            Map<Integer, SpellConfig> index = new LinkedHashMap<>();
            values.values().forEach(value -> index.put(value.mana(), value));
            return Map.copyOf(index);
        });
        JsonObject fireball = new JsonObject();
        fireball.addProperty("mana", 8);
        fireball.addProperty("cooldown", 120);
        JsonObject frostbolt = new JsonObject();
        frostbolt.addProperty("mana", 10);
        frostbolt.addProperty("cooldown", 80);

        reloader.apply(Map.of(id("spell/fireball"), fireball), null, null);
        assertEquals(new SpellConfig(8, 120), byMana.get().get(8));

        reloader.apply(Map.of(id("spell/frostbolt"), frostbolt), null, null);
        assertEquals(new SpellConfig(10, 80), byMana.get().get(10));
        assertTrue(byMana.get().get(8) == null);
    }

    @Test
    void rejectsBrokenTypedDatapackConfigWithFileIdInMessage() {
        PiDataConfigType<SpellConfig> spells = PiDataConfigType.create(
                "spell",
                spellCodec());
        PiConfigResourceReloader reloader = new PiConfigResourceReloader("pi_config");
        reloader.register(spells);
        JsonObject bad = new JsonObject();
        bad.addProperty("mana", "bad");
        bad.addProperty("cooldown", 120);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> reloader.apply(Map.of(id("spell/fireball"), bad), null, null));

        assertTrue(exception.getMessage().contains("Failed to decode datapack config spell/example:fireball"));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("example", path);
    }

    private static Codec<SpellConfig> spellCodec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("mana").forGetter(SpellConfig::mana),
                Codec.INT.fieldOf("cooldown").forGetter(SpellConfig::cooldown)
        ).apply(instance, SpellConfig::new));
    }

    private record SpellConfig(int mana, int cooldown) {
        SpellConfig plus(SpellConfig other) {
            return new SpellConfig(mana + other.mana, cooldown + other.cooldown);
        }
    }
}
