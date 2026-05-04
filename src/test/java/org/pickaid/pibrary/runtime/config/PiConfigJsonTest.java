package org.pickaid.pibrary.runtime.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.config.PiConfigEntry;
import org.pickaid.pibrary.api.config.PiConfigScope;
import org.pickaid.pibrary.api.config.PiConfigSpec;
import org.pickaid.pibrary.api.config.PiConfigValues;
import org.pickaid.pibrary.api.config.PiConfigValidators;

class PiConfigJsonTest {
    @Test
    void writesAndReadsNestedConfigValues() {
        PiConfigEntry<Integer> maxEnergy = PiConfigEntry
                .builder(id("gameplay/machines/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 100)
                .alsoValidate(PiConfigValidators.intRange(1, 10_000))
                .build();
        PiConfigEntry<Point> origin = PiConfigEntry
                .builder(id("gameplay/machines/origin"), pointCodec(), PiConfigScope.COMMON_BOOTSTRAP, new Point(0, 0))
                .build();
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry(maxEnergy)
                .entry(origin)
                .build();
        PiConfigValues values = PiConfigValues.builder(spec)
                .set(maxEnergy, 250)
                .set(origin, new Point(3, 7))
                .build();

        JsonObject json = PiConfigJson.write(values);
        assertEquals(250, json.getAsJsonObject("machines").get("max_energy").getAsInt());
        assertEquals(3, json.getAsJsonObject("machines").getAsJsonObject("origin").get("x").getAsInt());

        PiConfigValues decoded = PiConfigJson.read(spec, json);
        assertEquals(250, decoded.get(maxEnergy));
        assertEquals(new Point(3, 7), decoded.get(origin));
    }

    @Test
    void canWriteOnlyOverridesForCompactLocalFiles() {
        PiConfigEntry<Integer> maxEnergy = PiConfigEntry
                .builder(id("gameplay/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 100)
                .build();
        PiConfigEntry<String> starterSpell = PiConfigEntry
                .builder(id("gameplay/starter_spell"), Codec.STRING, PiConfigScope.COMMON_BOOTSTRAP, "example:fireball")
                .build();
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry(maxEnergy)
                .entry(starterSpell)
                .build();
        PiConfigValues values = PiConfigValues.builder(spec)
                .set(maxEnergy, 400)
                .build();

        JsonObject json = PiConfigJson.writeOverrides(values);

        assertEquals(400, json.get("max_energy").getAsInt());
        assertFalse(json.has("starter_spell"));
    }

    @Test
    void reportsCodecAndValidationErrorsTogether() {
        PiConfigEntry<Integer> maxEnergy = PiConfigEntry
                .builder(id("gameplay/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 100)
                .alsoValidate(PiConfigValidators.intRange(1, 10_000))
                .build();
        PiConfigEntry<Point> origin = PiConfigEntry
                .builder(id("gameplay/origin"), pointCodec(), PiConfigScope.COMMON_BOOTSTRAP, new Point(0, 0))
                .build();
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry(maxEnergy)
                .entry(origin)
                .build();
        JsonObject json = new JsonObject();
        json.addProperty("max_energy", 0);
        json.addProperty("origin", "bad");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PiConfigJson.read(spec, json));

        assertEquals(
                "invalid config json: example:gameplay/max_energy: invalid config value for example:gameplay/max_energy: must be inside [1, 10000]; "
                        + "example:gameplay/origin: Not a JSON object: \"bad\"",
                exception.getMessage()
        );
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("example", path);
    }

    private static Codec<Point> pointCodec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("x").forGetter(Point::x),
                Codec.INT.fieldOf("y").forGetter(Point::y)
        ).apply(instance, Point::new));
    }

    private record Point(int x, int y) {
    }
}
