package org.pickaid.pibrary.api.registrate;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.IForgeRegistry;
import org.pickaid.pibrary.api.render.tint.PiBlockTints;
import org.pickaid.pibrary.api.render.tint.PiItemTints;
import org.pickaid.pibrary.api.render.tint.PiTintRegistry;
import org.pickaid.pibrary.api.config.PiDataConfigCollector;
import org.pickaid.pibrary.api.config.PiDataConfigType;

@SuppressWarnings({"unchecked", "unused"})
final class PiRegistrateUsageSample {
    private static final PiDataConfigType<SpellRules> SPELL_RULES =
            PiDataConfigType.merged("spell", SpellRules.CODEC, SpellRules::mergeAll);

    private PiRegistrateUsageSample() {
    }

    static BlockBuilder<Block, PiRegistrate> relayCore(PiRegistrate registrate) {
        return registrate.block("relay_core", Block::new)
                .initialProperties(PiBlockProps::metal)
                .properties(properties -> properties
                        .noOcclusion()
                        .isValidSpawn((state, level, pos, type) -> false))
                .blockAndItemTag(Tags.Blocks.STORAGE_BLOCKS_IRON, Tags.Items.STORAGE_BLOCKS_IRON)
                .blockTags(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_IRON_TOOL)
                .section("machines")
                .onRegister(block -> {
                });
    }

    static ItemBuilder<Item, PiRegistrate> copperWand(PiRegistrate registrate) {
        return registrate.item("copper_wand", Item::new)
                .stacksTo(1)
                .fireResistant()
                .itemTags(Tags.Items.INGOTS_COPPER)
                .section("materials");
    }

    static NoConfigBuilder<SoundEvent, SoundEvent, PiRegistrate> relayOpenSound(PiRegistrate registrate) {
        return registrate.generic(
                "relay_open",
                Registries.SOUND_EVENT,
                () -> SoundEvent.createVariableRangeEvent(registrate.loc("relay_open"))
        ).onRegister(sound -> {
        });
    }

    static NoConfigBuilder<SpellType, SpellType, ExampleRegistrate> fireballSpell(ExampleRegistrate registrate) {
        return registrate.spell("fireball", SpellType::new, id -> new SpellRules(8, 120, 4))
                .onRegister(spell -> {
                });
    }

    static ItemBuilder<Item, PiRegistrate> spellScrollFromRegistry(
            PiRegistrate registrate,
            IForgeRegistry<SpellType> spells
    ) {
        return registrate.item("spell_scroll", Item::new)
                .section("scrolls")
                .variants(
                        () -> spells.getValues().stream()
                                .filter(SpellType::enabled)
                                .sorted(Comparator.comparing(spell -> spellId(spells, spell).toString()))
                                .toList(),
                        spell -> spellId(spells, spell).getPath(),
                        (stack, spell) -> stack.getOrCreateTag()
                                .putString("spell", spellId(spells, spell).toString()))
                .searchOnly()
                .stacksTo(1);
    }

    static PiTintRegistry configureTints(PiRegistrate registrate) {
        registrate.tintBlock(() -> relayCore(registrate).getEntry(), 0x44AAFF)
                .tintFoliage(() -> relayCore(registrate).getEntry())
                .tintBlockItem(() -> relayCore(registrate).getEntry())
                .tintItem(() -> Items.STICK, 0, 0x44AAFF)
                .tintDurability(() -> Items.STICK, 1, 0xAA2222, 0x22AAFF)
                .tintBlock(() -> relayCore(registrate).getEntry(), tint -> tint
                        .layer(0).constant(0x112233)
                        .layer(1).provider(PiBlockTints.foliage()))
                .tintItem(() -> Items.STICK, tint -> tint
                        .layer(0).provider(PiItemTints.layer(0, 0x44AAFF))
                        .layer(1).durability(0xAA2222, 0x22AAFF));
        return registrate.tints();
    }

    /**
     * Downstream mods should put their own gameplay words here instead of
     * asking Pibrary to grow generic transforms for every possible block or
     * content family.
     */
    private static final class ExampleRegistrate extends PiBaseRegistrate<ExampleRegistrate> {
        private static final ResourceKey<Registry<SpellType>> SPELLS =
                ResourceKey.createRegistryKey(PiRegistrateUsageSample.id("spells"));

        private ExampleRegistrate(String modid) {
            super(modid);
        }

        static ExampleRegistrate createForSample(String modid) {
            return new ExampleRegistrate(modid);
        }

        NoConfigBuilder<SpellType, SpellType, ExampleRegistrate> spell(
                String name,
                NonNullSupplier<SpellType> factory
        ) {
            return generic(name, SPELLS, factory);
        }

        NoConfigBuilder<SpellType, SpellType, ExampleRegistrate> spell(
                String name,
                Supplier<SpellType> factory,
                Function<ResourceLocation, SpellRules> config
        ) {
            ResourceLocation id = loc(name);
            dataConfig(SPELL_RULES.entry(id, config.apply(id)));
            return generic(name, SPELLS, factory::get);
        }

        void collectGeneratedConfigs(PiDataConfigCollector collector) {
            collectDataConfigs(collector);
        }
    }

    private static net.minecraft.resources.ResourceLocation id(String path) {
        return new net.minecraft.resources.ResourceLocation("example", path);
    }

    private static ResourceLocation spellId(IForgeRegistry<SpellType> spells, SpellType spell) {
        return Objects.requireNonNull(spells.getKey(spell), "registered spell id");
    }

    private static final class SpellType {
        boolean enabled() {
            return true;
        }
    }

    private record SpellRules(int mana, int cooldown, int range) {
        private static final Codec<SpellRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("mana").forGetter(SpellRules::mana),
                Codec.INT.fieldOf("cooldown").forGetter(SpellRules::cooldown),
                Codec.INT.fieldOf("range").forGetter(SpellRules::range)
        ).apply(instance, SpellRules::new));

        private static SpellRules mergeAll(java.util.Collection<SpellRules> values) {
            int mana = 0;
            int cooldown = 0;
            int range = 0;
            for (SpellRules value : values) {
                mana += value.mana;
                cooldown += value.cooldown;
                range = Math.max(range, value.range);
            }
            return new SpellRules(mana, cooldown, range);
        }
    }
}
