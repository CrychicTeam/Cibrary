package org.pickaid.pibrary.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PiDataConfigTypeTest {
    @Test
    void buildsDatapackPathsLikeTypedConfigFamilies() {
        PiDataConfigType<SpellConfig> spells = PiDataConfigType.create("spell", spellCodec());

        assertEquals(id("spell/fireball"), spells.resourceId(id("fireball")));
        assertEquals(
                "data/example/example_config/spell/fireball.json",
                spells.generatedPath("example_config", id("fireball"))
        );
    }

    @Test
    void collectorSerializesGeneratedConfigAndRejectsDuplicates() {
        PiDataConfigType<SpellConfig> spells = PiDataConfigType.create("spell", spellCodec());
        PiDataConfigCollector collector = new PiDataConfigCollector("example_config");

        collector.add(spells, id("fireball"), new SpellConfig(8, 120));

        JsonObject json = collector.entries()
                .get("data/example/example_config/spell/fireball.json")
                .getAsJsonObject();
        assertEquals(8, json.get("mana").getAsInt());
        assertEquals(120, json.get("cooldown").getAsInt());
        assertThrows(IllegalArgumentException.class,
                () -> collector.add(spells, id("fireball"), new SpellConfig(10, 90)));
    }

    @Test
    void collectorAcceptsExplicitDataConfigEntries() {
        PiDataConfigType<SpellConfig> spells = PiDataConfigType.create("spell", spellCodec());
        PiDataConfigEntry<SpellConfig> fireball = spells.entry(id("fireball"), new SpellConfig(8, 120));
        PiDataConfigCollector collector = new PiDataConfigCollector("example_config");

        collector.add(fireball);

        assertEquals(spells, fireball.type());
        assertEquals(id("fireball"), fireball.id());
        assertEquals(id("spell/fireball"), fireball.resourceId());
        JsonObject json = collector.entries()
                .get("data/example/example_config/spell/fireball.json")
                .getAsJsonObject();
        assertEquals(8, json.get("mana").getAsInt());
    }

    @Test
    void mergedTypeUsesExplicitMergeFunction() {
        PiDataConfigType<SpellConfig> spells = PiDataConfigType.merged("spell", spellCodec(), values -> values.stream()
                .reduce(new SpellConfig(0, 0), SpellConfig::plus));

        assertEquals(new SpellConfig(18, 200), spells.merge(java.util.List.of(
                new SpellConfig(8, 120),
                new SpellConfig(10, 80))));
    }

    @Test
    void rejectsFolderNamesThatCannotBeResourcePathSegments() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PiDataConfigType.create("spell/default", spellCodec()));

        assertEquals("datapack config folder must be one path segment: spell/default", exception.getMessage());
    }

    static Codec<SpellConfig> spellCodec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("mana").forGetter(SpellConfig::mana),
                Codec.INT.fieldOf("cooldown").forGetter(SpellConfig::cooldown)
        ).apply(instance, SpellConfig::new));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("example", path);
    }

    record SpellConfig(int mana, int cooldown) {
        SpellConfig plus(SpellConfig other) {
            return new SpellConfig(mana + other.mana, cooldown + other.cooldown);
        }
    }
}
