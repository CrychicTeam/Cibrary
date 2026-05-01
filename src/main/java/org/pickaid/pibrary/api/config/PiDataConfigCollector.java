package org.pickaid.pibrary.api.config;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * Collects generated datapack config files.
 *
 * <p>Custom Registrate builders can add their default configs here during
 * registration, then a data provider writes the collected files to
 * {@code data/<namespace>/<directory>/<type>/<path>.json}.</p>
 */
public final class PiDataConfigCollector {
    private final String directory;
    private final Map<String, JsonElement> entries = new LinkedHashMap<>();

    public PiDataConfigCollector(String directory) {
        this.directory = requireDirectory(directory);
    }

    public String directory() {
        return directory;
    }

    public <T> PiDataConfigCollector add(PiDataConfigType<T> type, ResourceLocation id, T value) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(value, "value");
        return add(type.entry(id, value));
    }

    public <T> PiDataConfigCollector add(PiDataConfigEntry<T> entry) {
        Objects.requireNonNull(entry, "entry");
        String path = entry.generatedPath(directory);
        JsonElement json = require(entry.type().codec().encodeStart(JsonOps.INSTANCE, entry.value()), "encode datapack config " + path);
        JsonElement previous = entries.putIfAbsent(path, json);
        if (previous != null) {
            throw new IllegalArgumentException("duplicate generated datapack config: " + path);
        }
        return this;
    }

    public Map<String, JsonElement> entries() {
        return Map.copyOf(entries);
    }

    public static String requireDirectory(String directory) {
        String cleanDirectory = Objects.requireNonNull(directory, "directory").trim();
        if (cleanDirectory.isEmpty()) {
            throw new IllegalArgumentException("datapack config directory must not be blank");
        }
        if (cleanDirectory.contains("\\") || cleanDirectory.contains(":")) {
            throw new IllegalArgumentException("datapack config directory must be a resource path: " + directory);
        }
        return cleanDirectory;
    }

    private static <T> T require(DataResult<T> result, String action) {
        return result.resultOrPartial(message -> {
        }).orElseThrow(() -> new IllegalStateException("Failed to " + action + ": "
                + result.error().map(DataResult.PartialResult::message).orElse("unknown error")));
    }
}
