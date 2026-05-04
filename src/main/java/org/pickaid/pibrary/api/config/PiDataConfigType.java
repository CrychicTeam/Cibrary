package org.pickaid.pibrary.api.config;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.piserializekit.api.service.PiSerializer;

/**
 * One family of datapack JSON config files.
 *
 * <p>A type named {@code spell} under directory {@code example_config} reads
 * files such as {@code data/example/example_config/spell/fireball.json}. The
 * file id exposed to game code is {@code example:fireball}.</p>
 *
 * @param folder first path segment below the datapack config directory
 * @param codec codec used to read and write each file
 * @param merger optional runtime merge function for {@code getMerged()}
 * @param <T> config value type
 */
public record PiDataConfigType<T>(
        String folder,
        Codec<T> codec,
        Optional<PiDataConfigMerger<T>> merger
) {
    public PiDataConfigType {
        folder = requireFolder(folder);
        Objects.requireNonNull(codec, "codec");
        merger = Objects.requireNonNull(merger, "merger");
    }

    public static <T> PiDataConfigType<T> create(String folder, Codec<T> codec) {
        return new PiDataConfigType<>(folder, codec, Optional.empty());
    }

    public static <T> PiDataConfigType<T> create(String folder, PiSerializer<T> serializer) {
        Objects.requireNonNull(serializer, "serializer");
        return create(folder, serializer.valueCodec());
    }

    public static <T> PiDataConfigType<T> merged(
            String folder,
            Codec<T> codec,
            PiDataConfigMerger<T> merger
    ) {
        return new PiDataConfigType<>(folder, codec, Optional.of(Objects.requireNonNull(merger, "merger")));
    }

    public static <T> PiDataConfigType<T> merged(
            String folder,
            PiSerializer<T> serializer,
            PiDataConfigMerger<T> merger
    ) {
        Objects.requireNonNull(serializer, "serializer");
        return merged(folder, serializer.valueCodec(), merger);
    }

    public ResourceLocation resourceId(ResourceLocation entryId) {
        Objects.requireNonNull(entryId, "entryId");
        return new ResourceLocation(entryId.getNamespace(), folder + "/" + entryId.getPath());
    }

    public PiDataConfigEntry<T> entry(ResourceLocation id, T value) {
        return new PiDataConfigEntry<>(this, id, value);
    }

    public String generatedPath(String directory, ResourceLocation entryId) {
        String cleanDirectory = PiDataConfigCollector.requireDirectory(directory);
        Objects.requireNonNull(entryId, "entryId");
        return "data/" + entryId.getNamespace() + "/" + cleanDirectory + "/" + folder + "/" + entryId.getPath() + ".json";
    }

    public T merge(Collection<T> values) {
        return merger.orElseThrow(() -> new IllegalStateException("datapack config type has no merger: " + folder))
                .merge(values);
    }

    static String requireFolder(String folder) {
        String cleanFolder = Objects.requireNonNull(folder, "folder").trim();
        if (cleanFolder.isEmpty()) {
            throw new IllegalArgumentException("datapack config folder must not be blank");
        }
        if (cleanFolder.contains("/") || cleanFolder.contains("\\") || cleanFolder.contains(":")) {
            throw new IllegalArgumentException("datapack config folder must be one path segment: " + folder);
        }
        return cleanFolder;
    }
}
