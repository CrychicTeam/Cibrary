package org.pickaid.pibrary.api.signal;

public record PiSignalTarget(PiSignalEndpointKind kind, int entityId) {
    public static PiSignalTarget none() {
        return new PiSignalTarget(PiSignalEndpointKind.NONE, -1);
    }

    public static PiSignalTarget entity(int entityId) {
        return new PiSignalTarget(PiSignalEndpointKind.ENTITY, entityId);
    }
}
