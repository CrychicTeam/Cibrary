package org.pickaid.pibrary.runtime.chunk;

public interface PiChunkServiceRegistry {
    void register(PiGeneratedChunkServiceDescriptor<?, ?> descriptor);
}
