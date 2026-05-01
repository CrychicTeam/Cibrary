package org.pickaid.pibrary.api.config;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.piserializekit.api.service.PiSerializer;

/**
 * Typed config entry definition shared by loaders, generated docs, and runtime
 * systems that need stable config metadata.
 *
 * @param id logical config id
 * @param codec codec used to load and save the value
 * @param scope config storage scope
 * @param defaultValue default value used when no data exists
 * @param comments human-facing comments written near the config value
 * @param validator value validator used after decode and during spec bootstrap
 * @param <T> config value type
 */
public record PiConfigEntry<T>(
        ResourceLocation id,
        Codec<T> codec,
        PiConfigScope scope,
        T defaultValue,
        List<String> comments,
        PiConfigValidator<T> validator
) {
    public PiConfigEntry {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(codec, "codec");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(defaultValue, "defaultValue");
        comments = cleanComments(comments);
        validator = validator == null ? PiConfigValidator.alwaysValid() : validator;
    }

    public PiConfigEntry(ResourceLocation id, Codec<T> codec, PiConfigScope scope, T defaultValue) {
        this(id, codec, scope, defaultValue, List.of(), PiConfigValidator.alwaysValid());
    }

    public static <T> Builder<T> builder(ResourceLocation id, Codec<T> codec, PiConfigScope scope, T defaultValue) {
        return new Builder<>(id, codec, scope, defaultValue);
    }

    public static <T> Builder<T> builder(ResourceLocation id, PiSerializer<T> serializer, PiConfigScope scope, T defaultValue) {
        Objects.requireNonNull(serializer, "serializer");
        return builder(id, serializer.valueCodec(), scope, defaultValue);
    }

    public List<String> validate(T value) {
        Objects.requireNonNull(value, "value");
        return validator.validate(value).stream().toList();
    }

    public List<String> validateDefault() {
        return validate(defaultValue);
    }

    private static List<String> cleanComments(List<String> comments) {
        Objects.requireNonNull(comments, "comments");
        List<String> cleaned = new ArrayList<>(comments.size());
        for (String comment : comments) {
            String line = Objects.requireNonNull(comment, "comment").trim();
            if (!line.isEmpty()) {
                cleaned.add(line);
            }
        }
        return List.copyOf(cleaned);
    }

    public static final class Builder<T> {
        private final ResourceLocation id;
        private final Codec<T> codec;
        private final PiConfigScope scope;
        private final T defaultValue;
        private final List<String> comments = new ArrayList<>();
        private PiConfigValidator<T> validator = PiConfigValidator.alwaysValid();

        private Builder(ResourceLocation id, Codec<T> codec, PiConfigScope scope, T defaultValue) {
            this.id = Objects.requireNonNull(id, "id");
            this.codec = Objects.requireNonNull(codec, "codec");
            this.scope = Objects.requireNonNull(scope, "scope");
            this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
        }

        public Builder<T> comment(String comment) {
            comments.add(Objects.requireNonNull(comment, "comment"));
            return this;
        }

        public Builder<T> comments(List<String> comments) {
            this.comments.addAll(Objects.requireNonNull(comments, "comments"));
            return this;
        }

        public Builder<T> validator(PiConfigValidator<T> validator) {
            this.validator = Objects.requireNonNull(validator, "validator");
            return this;
        }

        public Builder<T> alsoValidate(PiConfigValidator<T> validator) {
            this.validator = this.validator.and(Objects.requireNonNull(validator, "validator"));
            return this;
        }

        public PiConfigEntry<T> build() {
            return new PiConfigEntry<>(id, codec, scope, defaultValue, comments, validator);
        }
    }
}
