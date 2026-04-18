package org.pickaid.pibrary.runtime.level;

public interface PiLevelServiceRegistry {
    void register(PiGeneratedLevelServiceDescriptor<?, ?> descriptor);
}
