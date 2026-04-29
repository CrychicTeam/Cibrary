package org.pickaid.pibrary.runtime.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.pickaid.pibrary.api.entity.PiEntityLifecycleHandler;
import org.pickaid.pibrary.api.entity.PiEntityLifecycleService;

/**
 * Default runtime implementation that dispatches entity lifecycle callbacks by
 * runtime-assignable entity type with cached handler resolution.
 */
public final class PiDefaultEntityLifecycleService implements PiEntityLifecycleService {
    private final List<Entry<?>> handlers = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<Class<?>, List<Entry<?>>> resolvedHandlers = new ConcurrentHashMap<>();

    @Override
    public <E extends Entity> void register(Class<E> entityType, PiEntityLifecycleHandler<? super E> handler) {
        handlers.add(new Entry<>(Objects.requireNonNull(entityType, "entityType"), Objects.requireNonNull(handler, "handler")));
        resolvedHandlers.clear();
    }

    @Override
    public void onJoinLevel(Entity entity, Level level, boolean loadedFromDisk) {
        for (Entry<?> entry : bindingsFor(entity.getClass())) {
            entry.onJoinLevel(entity, level, loadedFromDisk);
        }
    }

    @Override
    public void onEnterSection(Entity entity, SectionPos oldSection, SectionPos newSection) {
        for (Entry<?> entry : bindingsFor(entity.getClass())) {
            entry.onEnterSection(entity, oldSection, newSection);
        }
    }

    /**
     * Exposes resolved handlers for focused unit tests.
     *
     * @param entityType entity type to resolve
     * @return immutable matching handler list
     */
    List<PiEntityLifecycleHandler<?>> handlersFor(Class<? extends Entity> entityType) {
        List<PiEntityLifecycleHandler<?>> resolved = new ArrayList<>();
        for (Entry<?> entry : bindingsFor(entityType)) {
            resolved.add(entry.rawHandler());
        }
        return List.copyOf(resolved);
    }

    private List<Entry<?>> bindingsFor(Class<?> entityType) {
        Objects.requireNonNull(entityType, "entityType");
        return resolvedHandlers.computeIfAbsent(entityType, this::resolveHandlers);
    }

    private List<Entry<?>> resolveHandlers(Class<?> entityType) {
        List<Entry<?>> resolved = new ArrayList<>();
        for (Entry<?> entry : handlers) {
            if (entry.supports(entityType)) {
                resolved.add(entry);
            }
        }
        return List.copyOf(resolved);
    }

    private record Entry<E extends Entity>(Class<E> entityType, PiEntityLifecycleHandler<? super E> handler) {
        private void onJoinLevel(Entity entity, Level level, boolean loadedFromDisk) {
            E cast = cast(entity);
            if (cast != null) {
                handler.onJoinLevel(cast, level, loadedFromDisk);
            }
        }

        private void onEnterSection(Entity entity, SectionPos oldSection, SectionPos newSection) {
            E cast = cast(entity);
            if (cast != null) {
                handler.onEnterSection(cast, oldSection, newSection);
            }
        }

        private boolean supports(Class<?> resolvedType) {
            return entityType.isAssignableFrom(resolvedType);
        }

        private PiEntityLifecycleHandler<?> rawHandler() {
            return handler;
        }

        private E cast(Entity entity) {
            return entityType.isInstance(entity) ? entityType.cast(entity) : null;
        }
    }
}
