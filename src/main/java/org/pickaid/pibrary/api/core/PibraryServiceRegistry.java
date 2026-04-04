package org.pickaid.pibrary.api.core;

import java.util.Optional;

public interface PibraryServiceRegistry {
    <T> void register(PibraryServiceKey<T> key, T service);

    <T> Optional<T> find(PibraryServiceKey<T> key);

    default <T> T require(PibraryServiceKey<T> key) {
        return find(key).orElseThrow(() -> new IllegalStateException("Missing service: " + key.id()));
    }
}
