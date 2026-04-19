package org.pickaid.pibrary.api.registrate.registry;

import com.mojang.serialization.Codec;

public interface PiDatapackRegistryHandle<T> extends PiRegistryHandle<T> {
    Codec<T> networkCodec();

    boolean syncToClient();
}
