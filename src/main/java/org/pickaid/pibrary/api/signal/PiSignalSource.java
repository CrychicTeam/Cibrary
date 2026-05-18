package org.pickaid.pibrary.api.signal;

public record PiSignalSource(PiSignalEndpointKind kind, int entityId) {
    public static PiSignalSource none() {
        return new PiSignalSource(PiSignalEndpointKind.NONE, -1);
    }

    public static PiSignalSource entity(int entityId) {
        return new PiSignalSource(PiSignalEndpointKind.ENTITY, entityId);
    }
}
