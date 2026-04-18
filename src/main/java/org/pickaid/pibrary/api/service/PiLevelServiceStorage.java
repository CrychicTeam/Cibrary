package org.pickaid.pibrary.api.service;

import java.util.Optional;
import net.minecraft.server.level.ServerLevel;

/**
 * Runtime backend that resolves typed level-scoped services for one server level.
 */
public interface PiLevelServiceStorage {
    /**
     * Finds an existing service instance without forcing creation.
     *
     * @param level authoritative server level
     * @param descriptor typed level-service descriptor
     * @param <T> level service type
     * @return resolved level service, if already present
     */
    default <T extends PiStateLevelService<?>> Optional<T> find(ServerLevel level, PiLevelServiceDescriptor<T, ?> descriptor) {
        return Optional.of(resolve(level, descriptor));
    }

    /**
     * Resolves or creates the service instance stored for the given level and descriptor.
     *
     * @param level authoritative server level
     * @param descriptor typed level-service descriptor
     * @param <T> level service type
     * @return resolved level service
     */
    <T extends PiStateLevelService<?>> T resolve(ServerLevel level, PiLevelServiceDescriptor<T, ?> descriptor);
}
