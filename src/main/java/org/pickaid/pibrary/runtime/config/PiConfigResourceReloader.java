package org.pickaid.pibrary.runtime.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.pickaid.pibrary.api.config.PiDataConfigType;
import org.pickaid.pibrary.api.config.PiConfigScope;
import org.pickaid.pibrary.api.config.PiConfigSpec;
import org.pickaid.pibrary.api.config.PiConfigValues;

/**
 * Datapack resource loader for {@link PiConfigSpec}.
 *
 * <p>The loader scans one datapack directory once per reload and distributes
 * matching JSON files to the registered specs. A spec with id
 * {@code example:gameplay} registered under directory {@code pi_config} reads
 * {@code data/example/pi_config/gameplay.json}.</p>
 */
public final class PiConfigResourceReloader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<ResourceLocation, PiConfigResourceBinding> bindings = new LinkedHashMap<>();
    private final Map<String, PiDataConfigBinding<?>> dataBindings = new LinkedHashMap<>();
    private final String directory;

    public PiConfigResourceReloader(String directory) {
        super(GSON, org.pickaid.pibrary.api.config.PiDataConfigCollector.requireDirectory(directory));
        this.directory = org.pickaid.pibrary.api.config.PiDataConfigCollector.requireDirectory(directory);
    }

    public String directory() {
        return directory;
    }

    public PiConfigResourceBinding register(PiConfigSpec spec) {
        Objects.requireNonNull(spec, "spec");
        if (spec.scope() != PiConfigScope.SERVER_DATA_PACK) {
            throw new IllegalArgumentException("datapack config spec must use SERVER_DATA_PACK scope: " + spec.id());
        }
        PiConfigResourceBinding binding = new PiConfigResourceBinding(spec);
        PiConfigResourceBinding previous = bindings.putIfAbsent(spec.id(), binding);
        if (previous != null) {
            throw new IllegalArgumentException("duplicate datapack config spec id: " + spec.id());
        }
        return binding;
    }

    public <T> PiDataConfigBinding<T> register(PiDataConfigType<T> type) {
        Objects.requireNonNull(type, "type");
        PiDataConfigBinding<T> binding = new PiDataConfigBinding<>(type);
        PiDataConfigBinding<?> previous = dataBindings.putIfAbsent(type.folder(), binding);
        if (previous != null) {
            throw new IllegalArgumentException("duplicate datapack config type: " + type.folder());
        }
        return binding;
    }

    public Map<ResourceLocation, PiConfigResourceBinding> bindings() {
        return Map.copyOf(bindings);
    }

    public Map<String, PiDataConfigBinding<?>> dataBindings() {
        return Map.copyOf(dataBindings);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> prepared, ResourceManager manager, ProfilerFiller profiler) {
        Map<ResourceLocation, PiConfigValues> decoded = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, PiConfigResourceBinding> entry : bindings.entrySet()) {
            ResourceLocation id = entry.getKey();
            PiConfigSpec spec = entry.getValue().spec();
            JsonElement element = prepared.get(id);
            if (element == null) {
                decoded.put(id, PiConfigValues.defaults(spec));
                continue;
            }
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("datapack config " + id + " must be a JSON object");
            }
            decoded.put(id, PiConfigJson.read(spec, element.getAsJsonObject()));
        }
        decoded.forEach((id, values) -> bindings.get(id).update(values));
        applyDataTypes(prepared);
    }

    private void applyDataTypes(Map<ResourceLocation, JsonElement> prepared) {
        Map<String, Map<ResourceLocation, ?>> decoded = new LinkedHashMap<>();
        dataBindings.keySet().forEach(folder -> decoded.put(folder, new LinkedHashMap<>()));
        for (Map.Entry<ResourceLocation, JsonElement> entry : prepared.entrySet()) {
            String path = entry.getKey().getPath();
            int slash = path.indexOf('/');
            if (slash <= 0 || slash == path.length() - 1) {
                continue;
            }
            String folder = path.substring(0, slash);
            PiDataConfigBinding<?> binding = dataBindings.get(folder);
            if (binding == null) {
                continue;
            }
            ResourceLocation configId = ResourceLocation.fromNamespaceAndPath(
                    entry.getKey().getNamespace(),
                    path.substring(slash + 1));
            readDataType(decoded.get(folder), binding, configId, entry.getValue());
        }
        dataBindings.forEach((folder, binding) -> updateDataBinding(binding, decoded.get(folder)));
    }

    private static <T> void readDataType(
            Map<ResourceLocation, ?> decoded,
            PiDataConfigBinding<T> binding,
            ResourceLocation id,
            JsonElement element
    ) {
        @SuppressWarnings("unchecked")
        Map<ResourceLocation, T> typed = (Map<ResourceLocation, T>) decoded;
        T value = require(binding.type().codec().parse(JsonOps.INSTANCE, element),
                "decode datapack config " + binding.type().folder() + "/" + id);
        T previous = typed.putIfAbsent(id, value);
        if (previous != null) {
            throw new IllegalArgumentException("duplicate datapack config " + binding.type().folder() + "/" + id);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> void updateDataBinding(PiDataConfigBinding<T> binding, Map<ResourceLocation, ?> values) {
        binding.update((Map<ResourceLocation, T>) values);
    }

    private static <T> T require(DataResult<T> result, String action) {
        return result.resultOrPartial(message -> {
        }).orElseThrow(() -> new IllegalArgumentException("Failed to " + action + ": "
                + result.error().map(DataResult.PartialResult::message).orElse("unknown error")));
    }
}
