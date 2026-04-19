package org.pickaid.pibrary.api.registrate.registry;

import com.mojang.serialization.Codec;
import org.pickaid.pibrary.api.registrate.PiRegistrate;

public final class PiDatapackRegistryBuilder<T> {
    public PiDatapackRegistryBuilder(PiRegistrate owner, String path, Codec<T> directCodec, Codec<T> networkCodec) {
    }
}
