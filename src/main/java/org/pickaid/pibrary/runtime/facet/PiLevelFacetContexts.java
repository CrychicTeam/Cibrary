package org.pickaid.pibrary.runtime.facet;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServices;

public final class PiLevelFacetContexts {
    private static final Map<ServerLevel, PibraryServiceContext> SHARED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private PiLevelFacetContexts() {
    }

    public static PibraryServiceContext shared(ServerLevel level) {
        return SHARED.computeIfAbsent(level, ignored -> PibraryServices.root().child());
    }
}
