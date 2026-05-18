package org.pickaid.pibrary.api.signal;

public record PiSignalScope(PiSignalScopeKind kind, int entityId) {
    public static PiSignalScope serverOnly() {
        return new PiSignalScope(PiSignalScopeKind.SERVER_ONLY, -1);
    }

    public static PiSignalScope trackingEntity(int entityId) {
        return new PiSignalScope(PiSignalScopeKind.TRACKING_ENTITY, entityId);
    }
}
