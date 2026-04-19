package org.pickaid.pibrary.api.registrate.host;

import java.util.function.Function;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.api.service.PiLevelServiceContext;

public final class PiLevelServiceEntry<S, T> {
    public PiLevelServiceEntry(PiRegistrate owner, String path, Class<S> stateType, Function<PiLevelServiceContext, T> factory) {
    }
}
