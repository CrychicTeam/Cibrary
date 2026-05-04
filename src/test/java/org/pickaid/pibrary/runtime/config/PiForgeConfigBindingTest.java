package org.pickaid.pibrary.runtime.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.config.ModConfig;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.config.PiConfigEntry;
import org.pickaid.pibrary.api.config.PiConfigScope;
import org.pickaid.pibrary.api.config.PiConfigSpec;
import org.pickaid.pibrary.api.config.PiConfigValues;
import org.pickaid.pibrary.api.config.PiConfigValidators;

class PiForgeConfigBindingTest {
    @Test
    void buildsForgeSpecAndReadsTypedValues() {
        PiConfigEntry<Integer> maxEnergy = PiConfigEntry
                .builder(id("gameplay/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 100)
                .comment("Maximum stored energy.")
                .alsoValidate(PiConfigValidators.intRange(1, 10_000))
                .build();
        PiConfigEntry<String> starterSpell = PiConfigEntry
                .builder(id("gameplay/starter_spell"), Codec.STRING, PiConfigScope.COMMON_BOOTSTRAP, "example:fireball")
                .alsoValidate(PiConfigValidators.notBlank("starter spell must not be blank"))
                .build();
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry(maxEnergy)
                .entry(starterSpell)
                .build();

        PiForgeConfigBinding binding = PiForgeConfigBinding.build(spec);
        CommentedConfig config = CommentedConfig.inMemory();
        config.set("max_energy", 250);
        config.set("starter_spell", "example:frost");
        binding.forgeSpec().setConfig(config);

        assertEquals(ModConfig.Type.COMMON, binding.suggestedType());
        assertEquals(List.of("max_energy"), binding.path(maxEnergy));
        assertEquals(250, binding.get(maxEnergy));
        assertEquals("example:frost", binding.get(starterSpell));

        PiConfigValues values = binding.values();
        assertEquals(250, values.get(maxEnergy));
        assertEquals("example:frost", values.get(starterSpell));
        assertEquals("Maximum stored energy.", config.getComment("max_energy"));
    }

    @Test
    void letsForgeCorrectInvalidValuesBackToDefaults() {
        PiConfigEntry<Integer> maxEnergy = PiConfigEntry
                .builder(id("gameplay/max_energy"), Codec.INT, PiConfigScope.COMMON_BOOTSTRAP, 100)
                .alsoValidate(PiConfigValidators.intRange(1, 10_000))
                .build();
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry(maxEnergy)
                .build();

        PiForgeConfigBinding binding = PiForgeConfigBinding.build(spec);
        CommentedConfig config = CommentedConfig.inMemory();
        config.set("max_energy", 0);
        binding.forgeSpec().setConfig(config);

        assertEquals(Integer.valueOf(100), config.<Integer>get("max_energy"));
        assertEquals(100, binding.get(maxEnergy));
        assertTrue(binding.values().overrides().isEmpty());
    }

    @Test
    void supportsNestedForgePathsAndLists() {
        PiConfigEntry<List<String>> enabledSpells = PiConfigEntry
                .builder(id("gameplay/spells/enabled"), Codec.STRING.listOf(), PiConfigScope.COMMON_BOOTSTRAP, List.of("example:fireball"))
                .build();
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry(enabledSpells)
                .build();

        PiForgeConfigBinding binding = PiForgeConfigBinding.build(spec);
        CommentedConfig config = CommentedConfig.inMemory();
        config.set(List.of("spells", "enabled"), List.of("example:frost", "example:storm"));
        binding.forgeSpec().setConfig(config);

        assertEquals(List.of("spells", "enabled"), binding.path(enabledSpells));
        assertEquals(List.of("example:frost", "example:storm"), binding.get(enabledSpells));
    }

    @Test
    void rejectsDatapackSpecsForForgeBinding() {
        PiConfigSpec spec = PiConfigSpec.builder(id("spells"), PiConfigScope.SERVER_DATA_PACK)
                .entry("enabled", Codec.STRING, "example:fireball")
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PiForgeConfigBinding.build(spec));

        assertEquals("SERVER_DATA_PACK config specs are datapack driven and cannot be backed by ForgeConfigSpec", exception.getMessage());
    }

    @Test
    void rejectsObjectShapedCodecDefaultsBecauseForgeTomlCannotHoldThemAsOneValue() {
        Codec<Point> pointCodec = Codec.INT.fieldOf("x").xmap(Point::new, Point::x).codec();
        PiConfigEntry<Point> point = PiConfigEntry
                .builder(id("gameplay/point"), pointCodec, PiConfigScope.COMMON_BOOTSTRAP, new Point(3))
                .build();
        PiConfigSpec spec = PiConfigSpec.builder(id("gameplay"), PiConfigScope.COMMON_BOOTSTRAP)
                .entry(point)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PiForgeConfigBinding.build(spec));

        assertEquals(
                "Forge config value example:gameplay/point encodes as an object; use a JSON/datapack loader for that entry",
                exception.getMessage()
        );
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("example", path);
    }

    private record Point(int x) {
    }
}
