package org.pickaid.pibrary.runtime.chunk;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.level.chunk.LevelChunk;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServices;

public final class PiChunkServiceContexts {
    private static final Map<LevelChunk, PibraryServiceContext> SHARED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private PiChunkServiceContexts() {
    }

    public static PibraryServiceContext shared(LevelChunk chunk) {
        return SHARED.computeIfAbsent(chunk, ignored -> PibraryServices.root().child());
    }
}
