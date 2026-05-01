package org.pickaid.pibrary.runtime.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.pickaid.pibrary.api.config.PiDataConfigCollector;

/**
 * Data provider for {@link PiDataConfigCollector}.
 */
public abstract class PiDataConfigProvider implements DataProvider {
    private final PackOutput output;
    private final String directory;
    private final String name;

    protected PiDataConfigProvider(PackOutput output, String directory, String name) {
        this.output = output;
        this.directory = PiDataConfigCollector.requireDirectory(directory);
        this.name = name == null || name.isBlank() ? "Pi data configs" : name;
    }

    protected abstract void add(PiDataConfigCollector collector);

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        PiDataConfigCollector collector = new PiDataConfigCollector(directory);
        add(collector);
        Path root = output.getOutputFolder();
        List<CompletableFuture<?>> writes = new ArrayList<>();
        collector.entries().forEach((path, json) ->
                writes.add(DataProvider.saveStable(cache, json, root.resolve(path))));
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return name;
    }
}
