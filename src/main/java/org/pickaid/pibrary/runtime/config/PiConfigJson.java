package org.pickaid.pibrary.runtime.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.pickaid.pibrary.api.config.PiConfigEntry;
import org.pickaid.pibrary.api.config.PiConfigSpec;
import org.pickaid.pibrary.api.config.PiConfigValues;

/**
 * JSON read/write helpers for {@link PiConfigSpec}.
 *
 * <p>Use this for datapack files, generated defaults, or local files that need
 * full Codec support. Forge TOML should use {@link PiForgeConfigBinding}
 * instead.</p>
 */
public final class PiConfigJson {
    private PiConfigJson() {
    }

    public static JsonObject write(PiConfigValues values) {
        return write(values, true);
    }

    public static JsonObject writeOverrides(PiConfigValues values) {
        return write(values, false);
    }

    public static PiConfigValues read(PiConfigSpec spec, JsonObject object) {
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(object, "object");
        PiConfigValues.Builder builder = PiConfigValues.builder(spec);
        List<String> errors = new ArrayList<>();
        for (PiConfigEntry<?> entry : spec.entries()) {
            Optional<JsonElement> element = find(object, PiConfigPaths.entryPath(spec, entry));
            if (element.isEmpty()) {
                continue;
            }
            readEntry(builder, entry, element.get(), errors);
        }
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("invalid config json: " + String.join("; ", errors));
        }
        return builder.build();
    }

    private static JsonObject write(PiConfigValues values, boolean includeDefaults) {
        Objects.requireNonNull(values, "values");
        JsonObject object = new JsonObject();
        for (PiConfigEntry<?> entry : values.spec().entries()) {
            if (includeDefaults || values.hasOverride(entry)) {
                writeEntry(object, values, entry);
            }
        }
        return object;
    }

    private static <T> void writeEntry(JsonObject object, PiConfigValues values, PiConfigEntry<T> entry) {
        T value = values.get(entry);
        JsonElement encoded = require(entry.codec().encodeStart(JsonOps.INSTANCE, value), "encode config value for " + entry.id());
        put(object, PiConfigPaths.entryPath(values.spec(), entry), encoded);
    }

    private static <T> void readEntry(
            PiConfigValues.Builder builder,
            PiConfigEntry<T> entry,
            JsonElement element,
            List<String> errors
    ) {
        Optional<T> decoded = entry.codec().parse(JsonOps.INSTANCE, element).resultOrPartial(
                message -> errors.add(entry.id() + ": " + message)
        );
        if (decoded.isEmpty()) {
            return;
        }
        try {
            builder.set(entry, decoded.get());
        } catch (IllegalArgumentException exception) {
            errors.add(entry.id() + ": " + exception.getMessage());
        }
    }

    private static void put(JsonObject root, List<String> path, JsonElement value) {
        JsonObject cursor = root;
        for (int i = 0; i < path.size() - 1; i++) {
            String segment = path.get(i);
            JsonElement existing = cursor.get(segment);
            if (existing == null) {
                JsonObject child = new JsonObject();
                cursor.add(segment, child);
                cursor = child;
                continue;
            }
            if (!existing.isJsonObject()) {
                throw new IllegalArgumentException("config path segment is already a value: " + String.join("/", path.subList(0, i + 1)));
            }
            cursor = existing.getAsJsonObject();
        }
        cursor.add(path.get(path.size() - 1), value);
    }

    private static Optional<JsonElement> find(JsonObject root, List<String> path) {
        JsonObject cursor = root;
        for (int i = 0; i < path.size(); i++) {
            JsonElement element = cursor.get(path.get(i));
            if (element == null) {
                return Optional.empty();
            }
            if (i == path.size() - 1) {
                return Optional.of(element);
            }
            if (!element.isJsonObject()) {
                return Optional.empty();
            }
            cursor = element.getAsJsonObject();
        }
        return Optional.empty();
    }

    private static <T> T require(DataResult<T> result, String action) {
        return result.resultOrPartial(message -> {
        }).orElseThrow(() -> new IllegalStateException("Failed to " + action + ": "
                + result.error().map(DataResult.PartialResult::message).orElse("unknown error")));
    }
}
