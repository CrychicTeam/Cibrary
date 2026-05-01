package org.pickaid.pibrary.api.config;

import java.util.Collection;

/**
 * Merges all datapack files of one {@link PiDataConfigType} into a runtime view.
 *
 * @param <T> config value type
 */
@FunctionalInterface
public interface PiDataConfigMerger<T> {
    T merge(Collection<T> values);
}
