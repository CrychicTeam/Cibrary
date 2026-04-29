package org.pickaid.pibrary.runtime.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.api.entity.PiVehicleLifecycleHandler;
import org.pickaid.pibrary.api.entity.PiVehicleLifecycleService;

/**
 * Default runtime implementation that dispatches mount and dismount callbacks by
 * runtime-assignable entity type with cached handler resolution.
 */
public final class PiDefaultVehicleLifecycleService implements PiVehicleLifecycleService {
    private final List<Entry<?>> handlers = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<Class<?>, List<Entry<?>>> resolvedHandlers = new ConcurrentHashMap<>();

    @Override
    public <E extends Entity> void register(Class<E> entityType, PiVehicleLifecycleHandler<? super E> handler) {
        handlers.add(new Entry<>(Objects.requireNonNull(entityType, "entityType"), Objects.requireNonNull(handler, "handler")));
        resolvedHandlers.clear();
    }

    @Override
    public void onMount(Entity entity, @Nullable Entity vehicle, Level level) {
        for (Entry<?> entry : bindingsFor(entity.getClass())) {
            entry.onMount(entity, vehicle, level);
        }
    }

    @Override
    public void onDismount(Entity entity, @Nullable Entity vehicle, Level level) {
        for (Entry<?> entry : bindingsFor(entity.getClass())) {
            entry.onDismount(entity, vehicle, level);
        }
    }

    /**
     * Exposes resolved handlers for focused unit tests.
     *
     * @param entityType entity type to resolve
     * @return immutable matching handler list
     */
    List<PiVehicleLifecycleHandler<?>> handlersFor(Class<? extends Entity> entityType) {
        List<PiVehicleLifecycleHandler<?>> resolved = new ArrayList<>();
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

    private record Entry<E extends Entity>(Class<E> entityType, PiVehicleLifecycleHandler<? super E> handler) {
        private void onMount(Entity entity, @Nullable Entity vehicle, Level level) {
            E cast = cast(entity);
            if (cast != null) {
                handler.onMount(cast, vehicle, level);
            }
        }

        private void onDismount(Entity entity, @Nullable Entity vehicle, Level level) {
            E cast = cast(entity);
            if (cast != null) {
                handler.onDismount(cast, vehicle, level);
            }
        }

        private boolean supports(Class<?> resolvedType) {
            return entityType.isAssignableFrom(resolvedType);
        }

        private PiVehicleLifecycleHandler<?> rawHandler() {
            return handler;
        }

        private E cast(Entity entity) {
            return entityType.isInstance(entity) ? entityType.cast(entity) : null;
        }
    }
}
