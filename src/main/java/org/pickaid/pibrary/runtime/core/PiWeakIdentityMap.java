package org.pickaid.pibrary.runtime.core;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Weak-keyed identity map used by source- and owner-scoped runtime caches.
 *
 * @param <V> mapped value type
 */
public final class PiWeakIdentityMap<V> {
    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
    private final Map<IdentityReference, V> values = new HashMap<>();

    /**
     * Returns the stored value for the given key, when present.
     *
     * @param key identity key
     * @return stored value or {@code null}
     */
    public synchronized V get(Object key) {
        Objects.requireNonNull(key, "key");
        expungeStaleEntries();
        return values.get(new LookupReference(key));
    }

    /**
     * Returns whether the map already contains a value for the given key.
     *
     * @param key identity key
     * @return {@code true} when a value exists
     */
    public synchronized boolean containsKey(Object key) {
        Objects.requireNonNull(key, "key");
        expungeStaleEntries();
        return values.containsKey(new LookupReference(key));
    }

    /**
     * Returns an existing value or computes and remembers a new one atomically.
     *
     * @param key identity key
     * @param mappingFunction value supplier
     * @return existing or newly created value
     */
    public synchronized V computeIfAbsent(Object key, Function<Object, V> mappingFunction) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(mappingFunction, "mappingFunction");
        expungeStaleEntries();
        LookupReference lookup = new LookupReference(key);
        V existing = values.get(lookup);
        if (existing != null) {
            return existing;
        }
        V value = mappingFunction.apply(key);
        values.put(new WeakIdentityReference(key, queue), value);
        return value;
    }

    private void expungeStaleEntries() {
        WeakIdentityReference cleared = (WeakIdentityReference) queue.poll();
        while (cleared != null) {
            values.remove(cleared);
            cleared = (WeakIdentityReference) queue.poll();
        }
    }

    private interface IdentityReference {
        Object referent();

        int identityHash();
    }

    private static final class LookupReference implements IdentityReference {
        private final Object referent;
        private final int identityHash;

        private LookupReference(Object referent) {
            this.referent = Objects.requireNonNull(referent, "referent");
            this.identityHash = System.identityHashCode(referent);
        }

        @Override
        public Object referent() {
            return referent;
        }

        @Override
        public int identityHash() {
            return identityHash;
        }

        @Override
        public int hashCode() {
            return identityHash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof IdentityReference other)) {
                return false;
            }
            return identityHash == other.identityHash() && referent == other.referent();
        }
    }

    private static final class WeakIdentityReference extends WeakReference<Object> implements IdentityReference {
        private final int identityHash;

        private WeakIdentityReference(Object referent, ReferenceQueue<Object> queue) {
            super(Objects.requireNonNull(referent, "referent"), queue);
            this.identityHash = System.identityHashCode(referent);
        }

        @Override
        public Object referent() {
            return get();
        }

        @Override
        public int identityHash() {
            return identityHash;
        }

        @Override
        public int hashCode() {
            return identityHash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof IdentityReference other)) {
                return false;
            }
            return identityHash == other.identityHash() && referent() == other.referent();
        }
    }
}
