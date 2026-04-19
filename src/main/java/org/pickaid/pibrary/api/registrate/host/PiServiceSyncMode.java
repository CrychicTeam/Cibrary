package org.pickaid.pibrary.api.registrate.host;

public enum PiServiceSyncMode {
    NONE("none"),
    OWNER("owner"),
    TRACKING("tracking");

    private final String id;

    PiServiceSyncMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
