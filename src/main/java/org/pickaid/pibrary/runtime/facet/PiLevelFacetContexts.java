package org.pickaid.pibrary.runtime.facet;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import org.pickaid.pibrary.api.core.PibraryScope;
import org.pickaid.pibrary.api.core.PibraryScopes;

public final class PiLevelFacetContexts {
    private static final Map<ServerLevel, PibraryScope> SHARED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private PiLevelFacetContexts() {
    }

    public static PibraryScope shared(ServerLevel level) {
        return SHARED.computeIfAbsent(level, ignored -> PibraryScopes.root().child());
    }
}
