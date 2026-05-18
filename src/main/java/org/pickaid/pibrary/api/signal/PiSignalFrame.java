package org.pickaid.pibrary.api.signal;

import java.util.Objects;

public record PiSignalFrame<T>(
        PiSignalType<T> type,
        PiSignalSource source,
        PiSignalTarget target,
        PiSignalScope scope,
        PiSignalPriority priority,
        long gameTime,
        T payload
) implements PiSignal {
    public PiSignalFrame {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(scope, "scope");
        Objects.requireNonNull(priority, "priority");
    }

    public static <T> Builder<T> builder(PiSignalType<T> type) {
        return new Builder<>(type);
    }

    public static final class Builder<T> {
        private final PiSignalType<T> type;
        private PiSignalSource source = PiSignalSource.none();
        private PiSignalTarget target = PiSignalTarget.none();
        private PiSignalScope scope = PiSignalScope.serverOnly();
        private PiSignalPriority priority = PiSignalPriority.NORMAL;
        private long gameTime;
        private T payload;

        private Builder(PiSignalType<T> type) {
            this.type = Objects.requireNonNull(type, "type");
        }

        public Builder<T> source(PiSignalSource source) {
            this.source = Objects.requireNonNull(source, "source");
            return this;
        }

        public Builder<T> target(PiSignalTarget target) {
            this.target = Objects.requireNonNull(target, "target");
            return this;
        }

        public Builder<T> scope(PiSignalScope scope) {
            this.scope = Objects.requireNonNull(scope, "scope");
            return this;
        }

        public Builder<T> priority(PiSignalPriority priority) {
            this.priority = Objects.requireNonNull(priority, "priority");
            return this;
        }

        public Builder<T> gameTime(long gameTime) {
            this.gameTime = gameTime;
            return this;
        }

        public Builder<T> payload(T payload) {
            this.payload = payload;
            return this;
        }

        public PiSignalFrame<T> build() {
            return new PiSignalFrame<>(type, source, target, scope, priority, gameTime, payload);
        }
    }
}
