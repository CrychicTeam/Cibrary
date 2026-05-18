package org.pickaid.pibrary.api.signal;

public interface PiSignal {
    PiSignalType<?> type();

    PiSignalSource source();

    PiSignalTarget target();

    PiSignalScope scope();

    PiSignalPriority priority();

    long gameTime();

    Object payload();
}
