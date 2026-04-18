package org.pickaid.pibrary.runtime.level;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import org.pickaid.pibrary.api.core.PibraryServiceContext;
import org.pickaid.pibrary.api.core.PibraryServices;

public final class PiLevelServiceContexts {
    private static final Map<ServerLevel, PibraryServiceContext> SHARED =
            Collections.synchronizedMap(new WeakHashMap<>());

    private PiLevelServiceContexts() {
    }

    public static PibraryServiceContext shared(ServerLevel level) {
        return SHARED.computeIfAbsent(level, ignored -> PibraryServices.root().child());
    }
}
