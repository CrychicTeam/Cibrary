package org.pickaid.pibrary.runtime.config;

import com.electronwill.nightconfig.core.Config;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.pickaid.pibrary.api.config.PiConfigEntry;
import org.pickaid.pibrary.api.config.PiConfigScope;
import org.pickaid.pibrary.api.config.PiConfigSpec;
import org.pickaid.pibrary.api.config.PiConfigValues;

/**
 * Forge runtime binding for a {@link PiConfigSpec}.
 *
 * <p>The binding keeps the public game code small: define entries once, pass the
 * generated {@link #forgeSpec()} to Forge's config registration, then read values
 * back through the same typed {@link PiConfigEntry} handles.</p>
 */
public final class PiForgeConfigBinding {
    private final PiConfigSpec spec;
    private final ForgeConfigSpec forgeSpec;
    private final Map<ResourceLocation, ForgeConfigSpec.ConfigValue<?>> values;
    private final Map<ResourceLocation, List<String>> paths;
    private final ModConfig.Type suggestedType;

    private PiForgeConfigBinding(
            PiConfigSpec spec,
            ForgeConfigSpec forgeSpec,
            Map<ResourceLocation, ForgeConfigSpec.ConfigValue<?>> values,
            Map<ResourceLocation, List<String>> paths,
            ModConfig.Type suggestedType
    ) {
        this.spec = Objects.requireNonNull(spec, "spec");
        this.forgeSpec = Objects.requireNonNull(forgeSpec, "forgeSpec");
        this.values = Map.copyOf(Objects.requireNonNull(values, "values"));
        this.paths = Map.copyOf(Objects.requireNonNull(paths, "paths"));
        this.suggestedType = Objects.requireNonNull(suggestedType, "suggestedType");
    }

    public static PiForgeConfigBinding build(PiConfigSpec spec) {
        Objects.requireNonNull(spec, "spec");
        ModConfig.Type suggestedType = suggestForgeType(spec.scope());
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        Map<ResourceLocation, ForgeConfigSpec.ConfigValue<?>> values = new LinkedHashMap<>();
        Map<ResourceLocation, List<String>> paths = new LinkedHashMap<>();
        for (PiConfigEntry<?> entry : spec.entries()) {
            List<String> path = PiConfigPaths.entryPath(spec, entry);
            if (!entry.comments().isEmpty()) {
                builder.comment(entry.comments().toArray(String[]::new));
            }
            ForgeConfigSpec.ConfigValue<?> value = define(builder, path, entry);
            values.put(entry.id(), value);
            paths.put(entry.id(), path);
        }
        return new PiForgeConfigBinding(spec, builder.build(), values, paths, suggestedType);
    }

    public PiConfigSpec spec() {
        return spec;
    }

    public ForgeConfigSpec forgeSpec() {
        return forgeSpec;
    }

    /**
     * Suggested Forge config type for this spec.
     *
     * <p>Callers can still register the spec manually if a mod has unusual file
     * placement needs.</p>
     */
    public ModConfig.Type suggestedType() {
        return suggestedType;
    }

    public List<String> path(PiConfigEntry<?> entry) {
        return paths.get(requireEntry(entry).id());
    }

    public ForgeConfigSpec.ConfigValue<?> forgeValue(PiConfigEntry<?> entry) {
        return values.get(requireEntry(entry).id());
    }

    public <T> T get(PiConfigEntry<T> entry) {
        PiConfigEntry<T> registeredEntry = requireEntry(entry);
        Object raw = values.get(registeredEntry.id()).get();
        return decode(registeredEntry, raw);
    }

    public PiConfigValues values() {
        PiConfigValues.Builder builder = PiConfigValues.builder(spec);
        for (PiConfigEntry<?> entry : spec.entries()) {
            setDecoded(builder, entry, values.get(entry.id()).get());
        }
        return builder.build();
    }

    private static ModConfig.Type suggestForgeType(PiConfigScope scope) {
        return switch (scope) {
            case COMMON_BOOTSTRAP -> ModConfig.Type.COMMON;
            case CLIENT_LOCAL -> ModConfig.Type.CLIENT;
            case SERVER_DATA_PACK -> throw new IllegalArgumentException(
                    "SERVER_DATA_PACK config specs are datapack driven and cannot be backed by ForgeConfigSpec"
            );
        };
    }

    private static ForgeConfigSpec.ConfigValue<?> define(
            ForgeConfigSpec.Builder builder,
            List<String> path,
            PiConfigEntry<?> entry
    ) {
        Object defaultValue = toForgeValue(entry, entry.defaultValue());
        return builder.define(path, defaultValue, raw -> canDecode(entry, raw));
    }

    private static boolean canDecode(PiConfigEntry<?> entry, Object raw) {
        try {
            decode(entry, raw);
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static <T> void setDecoded(PiConfigValues.Builder builder, PiConfigEntry<T> entry, Object raw) {
        T value = decode(entry, raw);
        if (!Objects.equals(value, entry.defaultValue())) {
            builder.set(entry, value);
        }
    }

    private static <T> T decode(PiConfigEntry<T> entry, Object raw) {
        T value = decodeRaw(entry, raw);
        List<String> errors = entry.validate(value);
        if (!errors.isEmpty()) {
            throw new IllegalStateException("invalid Forge config value for " + entry.id() + ": " + String.join("; ", errors));
        }
        return value;
    }

    private static <T> T decodeRaw(PiConfigEntry<T> entry, Object raw) {
        JsonElement json = toJson(raw);
        return require(entry.codec().parse(JsonOps.INSTANCE, json), "decode Forge config value for " + entry.id());
    }

    private static <T> Object toForgeValue(PiConfigEntry<T> entry, Object value) {
        @SuppressWarnings("unchecked")
        T typedValue = (T) value;
        JsonElement json = require(entry.codec().encodeStart(JsonOps.INSTANCE, typedValue), "encode Forge config default for " + entry.id());
        return toForgeValue(entry.id(), json);
    }

    private static Object toForgeValue(ResourceLocation id, JsonElement json) {
        if (json == null || json instanceof JsonNull) {
            throw new IllegalArgumentException("Forge config value cannot be null: " + id);
        }
        if (json instanceof JsonPrimitive primitive) {
            return primitiveToForgeValue(id, primitive);
        }
        if (json instanceof JsonArray array) {
            List<Object> values = new ArrayList<>(array.size());
            for (JsonElement element : array) {
                values.add(toForgeValue(id, element));
            }
            return List.copyOf(values);
        }
        if (json instanceof JsonObject) {
            throw new IllegalArgumentException("Forge config value " + id + " encodes as an object; use a JSON/datapack loader for that entry");
        }
        throw new IllegalArgumentException("Unsupported Forge config JSON value for " + id + ": " + json);
    }

    private static Object primitiveToForgeValue(ResourceLocation id, JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean();
        }
        if (primitive.isString()) {
            return primitive.getAsString();
        }
        if (primitive.isNumber()) {
            return numberToForgeValue(id, primitive);
        }
        throw new IllegalArgumentException("Unsupported Forge config primitive for " + id + ": " + primitive);
    }

    private static Object numberToForgeValue(ResourceLocation id, JsonPrimitive primitive) {
        String text = primitive.getAsString();
        try {
            BigDecimal decimal = new BigDecimal(text);
            if (decimal.stripTrailingZeros().scale() <= 0) {
                long value = decimal.longValueExact();
                if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                    return (int) value;
                }
                return value;
            }
            double value = decimal.doubleValue();
            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Forge config number must be finite for " + id + ": " + text);
            }
            return value;
        } catch (ArithmeticException | NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid Forge config number for " + id + ": " + text, exception);
        }
    }

    private static JsonElement toJson(Object value) {
        if (value == null) {
            return JsonNull.INSTANCE;
        }
        if (value instanceof JsonElement json) {
            return json;
        }
        if (value instanceof Boolean bool) {
            return new JsonPrimitive(bool);
        }
        if (value instanceof Number number) {
            return new JsonPrimitive(number);
        }
        if (value instanceof String string) {
            return new JsonPrimitive(string);
        }
        if (value instanceof List<?> list) {
            JsonArray array = new JsonArray();
            for (Object element : list) {
                array.add(toJson(element));
            }
            return array;
        }
        if (value instanceof Config config) {
            return configToJson(config.valueMap());
        }
        if (value instanceof Map<?, ?> map) {
            return mapToJson(map);
        }
        throw new IllegalArgumentException("Unsupported Forge config value type: " + value.getClass().getName());
    }

    private static JsonObject configToJson(Map<String, Object> map) {
        JsonObject object = new JsonObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            object.add(entry.getKey(), toJson(entry.getValue()));
        }
        return object;
    }

    private static JsonObject mapToJson(Map<?, ?> map) {
        JsonObject object = new JsonObject();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            object.add(String.valueOf(entry.getKey()), toJson(entry.getValue()));
        }
        return object;
    }

    @SuppressWarnings("unchecked")
    private <T> PiConfigEntry<T> requireEntry(PiConfigEntry<T> entry) {
        PiConfigPaths.requireInSpec(spec, entry);
        return (PiConfigEntry<T>) spec.find(entry.id()).orElseThrow();
    }

    private static <T> T require(DataResult<T> result, String action) {
        return result.resultOrPartial(message -> {
        }).orElseThrow(() -> new IllegalStateException("Failed to " + action + ": "
                + result.error().map(DataResult.PartialResult::message).orElse("unknown error")));
    }
}
