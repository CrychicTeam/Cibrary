package org.pickaid.pibrary.dev.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.config.PiDataConfigCollector;
import org.pickaid.pibrary.dev.example.CounterSpellConfigs.EntitySpellRules;
import org.pickaid.pibrary.dev.example.CounterSpellConfigs.SpellRules;

class CounterSpellConfigsTest {
    @Test
    void datagenCollectorWritesBaseSpellAndEntityOverrideFiles() {
        PiDataConfigCollector collector = new PiDataConfigCollector("pibrary_config");

        CounterSpellConfigs.collectDefaults(collector);

        JsonObject fireball = collector.entries()
                .get("data/pibrary/pibrary_config/spell/fireball.json")
                .getAsJsonObject();
        JsonObject zombie = collector.entries()
                .get("data/pibrary/pibrary_config/spell_entity/fireball_zombie.json")
                .getAsJsonObject();
        assertEquals(8, fireball.get("mana").getAsInt());
        assertEquals("pibrary:fireball", zombie.get("spell").getAsString());
        assertEquals("minecraft:zombie", zombie.get("entity").getAsString());
        assertEquals(6, zombie.getAsJsonObject("rules").get("mana").getAsInt());
    }

    @Test
    void resolvesEntitySpecificRulesBeforeBaseRules() {
        ResourceLocation fireball = id("fireball");
        ResourceLocation zombie = minecraft("zombie");
        ResourceLocation skeleton = minecraft("skeleton");
        SpellRules base = new SpellRules(8, 120, 16);
        SpellRules override = new SpellRules(6, 80, 20);

        assertEquals(override, CounterSpellConfigs.resolveRules(
                fireball,
                zombie,
                Map.of(fireball, base),
                List.of(new EntitySpellRules(fireball, zombie, override))));
        assertEquals(base, CounterSpellConfigs.resolveRules(
                fireball,
                skeleton,
                Map.of(fireball, base),
                List.of(new EntitySpellRules(fireball, zombie, override))));
    }

    @Test
    void rejectsDuplicateEntityOverrideKeys() {
        ResourceLocation fireball = id("fireball");
        ResourceLocation zombie = minecraft("zombie");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CounterSpellConfigs.indexEntityRules(List.of(
                        new EntitySpellRules(fireball, zombie, new SpellRules(6, 80, 20)),
                        new EntitySpellRules(fireball, zombie, new SpellRules(4, 60, 12)))));

        assertEquals("duplicate entity spell config: pibrary:fireball for minecraft:zombie", exception.getMessage());
    }

    @Test
    void missingBaseSpellConfigFailsAtLookupBoundary() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CounterSpellConfigs.resolveRules(
                        id("missing"),
                        minecraft("zombie"),
                        Map.of(),
                        List.of()));

        assertEquals("missing spell config: pibrary:missing", exception.getMessage());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("pibrary", path);
    }

    private static ResourceLocation minecraft(String path) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", path);
    }
}
