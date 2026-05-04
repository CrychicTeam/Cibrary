package org.pickaid.pibrary.dev.example;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.pickaid.pibrary.api.config.PiDataConfigCollector;
import org.pickaid.pibrary.api.config.PiDataConfigType;
import org.pickaid.pibrary.runtime.config.PiConfigResourceReloader;
import org.pickaid.pibrary.runtime.config.PiDataConfigBinding;
import org.pickaid.pibrary.runtime.config.PiDataConfigView;

/**
 * Dev-only sample for spell configs loaded from datapacks.
 */
public final class CounterSpellConfigs {
    public static final PiDataConfigType<SpellRules> SPELLS =
            PiDataConfigType.create("spell", SpellRules.CODEC);
    public static final PiDataConfigType<EntitySpellRules> ENTITY_SPELLS =
            PiDataConfigType.create("spell_entity", EntitySpellRules.CODEC);

    public static final PiConfigResourceReloader RELOADER =
            new PiConfigResourceReloader("pibrary_config");
    public static final PiDataConfigBinding<SpellRules> SPELL_VALUES =
            RELOADER.register(SPELLS);
    public static final PiDataConfigBinding<EntitySpellRules> ENTITY_SPELL_VALUES =
            RELOADER.register(ENTITY_SPELLS);
    public static final PiDataConfigView<EntitySpellRules, Map<EntitySpellKey, SpellRules>> ENTITY_INDEX =
            ENTITY_SPELL_VALUES.view(values -> indexEntityRules(values.values()));

    private CounterSpellConfigs() {
    }

    public static void collectDefaults(PiDataConfigCollector collector) {
        collector.add(SPELLS, id("fireball"), new SpellRules(8, 120, 16));
        collector.add(SPELLS, id("frostbolt"), new SpellRules(10, 90, 12));
        collector.add(ENTITY_SPELLS, id("fireball_zombie"), new EntitySpellRules(
                id("fireball"),
                minecraft("zombie"),
                new SpellRules(6, 80, 20)));
    }

    public static SpellRules rulesFor(ResourceLocation spell, EntityType<?> entityType) {
        ResourceLocation entityId = Objects.requireNonNull(
                ForgeRegistries.ENTITY_TYPES.getKey(entityType),
                "registered entity type");
        return rulesFor(spell, entityId);
    }

    public static SpellRules rulesFor(ResourceLocation spell, ResourceLocation entityType) {
        return resolveRules(spell, entityType, SPELL_VALUES.entries(), ENTITY_INDEX.get());
    }

    public static SpellRules resolveRules(
            ResourceLocation spell,
            ResourceLocation entityType,
            Map<ResourceLocation, SpellRules> baseRules,
            Collection<EntitySpellRules> entityRules
    ) {
        return resolveRules(spell, entityType, baseRules, indexEntityRules(entityRules));
    }

    public static SpellRules resolveRules(
            ResourceLocation spell,
            ResourceLocation entityType,
            Map<ResourceLocation, SpellRules> baseRules,
            Map<EntitySpellKey, SpellRules> entityRules
    ) {
        Objects.requireNonNull(spell, "spell");
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(baseRules, "baseRules");
        Objects.requireNonNull(entityRules, "entityRules");
        SpellRules override = entityRules.get(new EntitySpellKey(spell, entityType));
        if (override != null) {
            return override;
        }
        SpellRules base = baseRules.get(spell);
        if (base == null) {
            throw new IllegalArgumentException("missing spell config: " + spell);
        }
        return base;
    }

    public static Map<EntitySpellKey, SpellRules> indexEntityRules(Collection<EntitySpellRules> rules) {
        Objects.requireNonNull(rules, "rules");
        Map<EntitySpellKey, SpellRules> index = new LinkedHashMap<>();
        for (EntitySpellRules rule : rules) {
            EntitySpellKey key = new EntitySpellKey(rule.spell(), rule.entity());
            SpellRules previous = index.putIfAbsent(key, rule.rules());
            if (previous != null) {
                throw new IllegalArgumentException("duplicate entity spell config: "
                        + rule.spell() + " for " + rule.entity());
            }
        }
        return Map.copyOf(index);
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("pibrary", path);
    }

    private static ResourceLocation minecraft(String path) {
        return new ResourceLocation("minecraft", path);
    }

    public record SpellRules(int mana, int cooldown, int range) {
        public static final Codec<SpellRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("mana").forGetter(SpellRules::mana),
                Codec.INT.fieldOf("cooldown").forGetter(SpellRules::cooldown),
                Codec.INT.fieldOf("range").forGetter(SpellRules::range)
        ).apply(instance, SpellRules::new));
    }

    public record EntitySpellRules(
            ResourceLocation spell,
            ResourceLocation entity,
            SpellRules rules
    ) {
        public static final Codec<EntitySpellRules> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("spell").forGetter(EntitySpellRules::spell),
                ResourceLocation.CODEC.fieldOf("entity").forGetter(EntitySpellRules::entity),
                SpellRules.CODEC.fieldOf("rules").forGetter(EntitySpellRules::rules)
        ).apply(instance, EntitySpellRules::new));

        public EntitySpellRules {
            Objects.requireNonNull(spell, "spell");
            Objects.requireNonNull(entity, "entity");
            Objects.requireNonNull(rules, "rules");
        }
    }

    public record EntitySpellKey(ResourceLocation spell, ResourceLocation entity) {
        public EntitySpellKey {
            Objects.requireNonNull(spell, "spell");
            Objects.requireNonNull(entity, "entity");
        }
    }
}
