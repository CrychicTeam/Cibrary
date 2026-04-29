package org.pickaid.pibrary.api.config;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;

/**
 * A grouped config definition for one logical file or data source.
 *
 * <p>A spec deliberately contains metadata only. Forge config, datapack JSON,
 * local client files, or a future Pi config loader can all consume the same
 * spec without forcing gameplay code to depend on one loader shape.</p>
 */
public record PiConfigSpec(
        ResourceLocation id,
        PiConfigScope scope,
        List<PiConfigEntry<?>> entries
) {
    public PiConfigSpec {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(scope, "scope");
        entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
        validateEntries(scope, entries);
        List<String> defaultErrors = validateDefaults(entries);
        if (!defaultErrors.isEmpty()) {
            throw new IllegalArgumentException("invalid config defaults: " + String.join("; ", defaultErrors));
        }
    }

    public static Builder builder(ResourceLocation id, PiConfigScope scope) {
        return new Builder(id, scope);
    }

    public Optional<PiConfigEntry<?>> find(ResourceLocation entryId) {
        Objects.requireNonNull(entryId, "entryId");
        return entries.stream().filter(entry -> entry.id().equals(entryId)).findFirst();
    }

    public List<String> validateDefaults() {
        return validateDefaults(entries);
    }

    private static void validateEntries(PiConfigScope scope, List<PiConfigEntry<?>> entries) {
        Map<ResourceLocation, PiConfigEntry<?>> seen = new LinkedHashMap<>();
        for (PiConfigEntry<?> entry : entries) {
            if (entry.scope() != scope) {
                throw new IllegalArgumentException("config entry " + entry.id() + " has scope " + entry.scope()
                        + " but spec scope is " + scope);
            }
            PiConfigEntry<?> previous = seen.putIfAbsent(entry.id(), entry);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate config entry id: " + entry.id());
            }
        }
    }

    private static List<String> validateDefaults(List<PiConfigEntry<?>> entries) {
        List<String> errors = new ArrayList<>();
        for (PiConfigEntry<?> entry : entries) {
            errors.addAll(validateDefault(entry));
        }
        return List.copyOf(errors);
    }

    private static <T> List<String> validateDefault(PiConfigEntry<T> entry) {
        return entry.validateDefault().stream()
                .map(message -> entry.id() + ": " + message)
                .toList();
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final PiConfigScope scope;
        private final List<PiConfigEntry<?>> entries = new ArrayList<>();

        private Builder(ResourceLocation id, PiConfigScope scope) {
            this.id = Objects.requireNonNull(id, "id");
            this.scope = Objects.requireNonNull(scope, "scope");
        }

        public <T> Builder entry(PiConfigEntry<T> entry) {
            entries.add(Objects.requireNonNull(entry, "entry"));
            return this;
        }

        public <T> Builder entry(String path, Codec<T> codec, T defaultValue) {
            return entry(path, codec, defaultValue, builder -> {
            });
        }

        public <T> Builder entry(
                String path,
                Codec<T> codec,
                T defaultValue,
                Consumer<PiConfigEntry.Builder<T>> configure
        ) {
            Objects.requireNonNull(configure, "configure");
            PiConfigEntry.Builder<T> builder = PiConfigEntry.builder(childId(path), codec, scope, defaultValue);
            configure.accept(builder);
            return entry(builder.build());
        }

        public PiConfigSpec build() {
            return new PiConfigSpec(id, scope, entries);
        }

        private ResourceLocation childId(String path) {
            String cleanPath = Objects.requireNonNull(path, "path").trim();
            if (cleanPath.isEmpty()) {
                throw new IllegalArgumentException("config entry path must not be blank");
            }
            return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + "/" + cleanPath);
        }
    }
}
