package org.pickaid.pibrary.runtime.config;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.pickaid.pibrary.api.config.PiConfigEntry;
import org.pickaid.pibrary.api.config.PiConfigSpec;

final class PiConfigPaths {
    private PiConfigPaths() {
    }

    static List<String> entryPath(PiConfigSpec spec, PiConfigEntry<?> entry) {
        requireInSpec(spec, entry);
        String specPath = spec.id().getPath();
        String entryPath = entry.id().getPath();
        String prefix = specPath + "/";
        if (!entryPath.startsWith(prefix)) {
            throw new IllegalArgumentException("config entry " + entry.id() + " must be below spec path " + spec.id());
        }
        String relative = entryPath.substring(prefix.length());
        if (relative.isBlank()) {
            throw new IllegalArgumentException("config entry " + entry.id() + " has no config path");
        }
        return Arrays.stream(relative.split("/"))
                .map(String::trim)
                .filter(segment -> !segment.isEmpty())
                .toList();
    }

    static void requireInSpec(PiConfigSpec spec, PiConfigEntry<?> entry) {
        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(entry, "entry");
        Optional<PiConfigEntry<?>> registered = spec.find(entry.id());
        if (registered.isEmpty() || !registered.get().equals(entry)) {
            throw new IllegalArgumentException("config entry does not belong to spec: " + entry.id());
        }
        if (!entry.id().getNamespace().equals(spec.id().getNamespace())) {
            throw new IllegalArgumentException("config entry " + entry.id() + " must use spec namespace " + spec.id().getNamespace());
        }
    }
}
