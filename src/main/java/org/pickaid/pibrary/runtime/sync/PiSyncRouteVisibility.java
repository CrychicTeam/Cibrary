package org.pickaid.pibrary.runtime.sync;

import org.pickaid.pinet.api.sync.model.PiSyncRoute;
import org.pickaid.piserializekit.api.schema.PiFieldDescriptor;
import org.pickaid.piserializekit.api.schema.PiProjection;
import org.pickaid.piserializekit.api.schema.PiSyncScope;

/**
 * Shared route-to-scope visibility rules for generated host-state sync paths.
 */
public final class PiSyncRouteVisibility {
    private PiSyncRouteVisibility() {
    }

    /**
     * Creates a projection that includes only fields visible on the given route.
     *
     * @param route target sync route
     * @return field projection for that route
     */
    public static PiProjection projection(PiSyncRoute route) {
        return field -> includes(field, route);
    }

    /**
     * Returns whether one field descriptor is visible on the requested route.
     *
     * @param field field descriptor to test
     * @param route target sync route
     * @return {@code true} when the field is visible
     */
    public static boolean includes(PiFieldDescriptor field, PiSyncRoute route) {
        return includes(field.syncScope(), route);
    }

    /**
     * Returns whether one sync scope is visible on the requested route.
     *
     * @param scope field sync scope
     * @param route target sync route
     * @return {@code true} when the scope is visible
     */
    public static boolean includes(PiSyncScope scope, PiSyncRoute route) {
        return switch (route) {
            case OWNER, PLAYER -> switch (scope) {
                case OWNER, TRACKING, CHUNK, GLOBAL -> true;
                case NONE, MENU -> false;
            };
            case TRACKING -> switch (scope) {
                case TRACKING, CHUNK, GLOBAL -> true;
                case NONE, OWNER, MENU -> false;
            };
            case CHUNK -> scope == PiSyncScope.CHUNK || scope == PiSyncScope.GLOBAL;
            case MENU -> switch (scope) {
                case MENU, OWNER, TRACKING, CHUNK, GLOBAL -> true;
                case NONE -> false;
            };
            case GLOBAL -> scope == PiSyncScope.GLOBAL;
        };
    }
}
