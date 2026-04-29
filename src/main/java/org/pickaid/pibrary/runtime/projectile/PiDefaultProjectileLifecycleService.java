package org.pickaid.pibrary.runtime.projectile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.world.entity.projectile.Projectile;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleContext;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleHandler;
import org.pickaid.pibrary.api.projectile.PiProjectileLifecycleService;

/**
 * Default runtime implementation that dispatches projectile lifecycle callbacks
 * by runtime-assignable projectile type with cached handler resolution.
 */
public final class PiDefaultProjectileLifecycleService implements PiProjectileLifecycleService {
    private final List<Entry<?>> handlers = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<Class<?>, List<Entry<?>>> resolvedHandlers = new ConcurrentHashMap<>();

    @Override
    public <P extends Projectile> void register(Class<P> projectileType, PiProjectileLifecycleHandler<? super P> handler) {
        handlers.add(new Entry<>(Objects.requireNonNull(projectileType, "projectileType"), Objects.requireNonNull(handler, "handler")));
        resolvedHandlers.clear();
    }

    @Override
    public void onJoinLevel(Projectile projectile, PiProjectileLifecycleContext context) {
        for (Entry<?> entry : bindingsFor(projectile.getClass())) {
            entry.onJoinLevel(projectile, context);
        }
    }

    /**
     * Exposes resolved handlers for focused unit tests.
     *
     * @param projectileType projectile type to resolve
     * @return immutable matching handler list
     */
    List<PiProjectileLifecycleHandler<?>> handlersFor(Class<? extends Projectile> projectileType) {
        List<PiProjectileLifecycleHandler<?>> resolved = new ArrayList<>();
        for (Entry<?> entry : bindingsFor(projectileType)) {
            resolved.add(entry.rawHandler());
        }
        return List.copyOf(resolved);
    }

    private List<Entry<?>> bindingsFor(Class<?> projectileType) {
        Objects.requireNonNull(projectileType, "projectileType");
        return resolvedHandlers.computeIfAbsent(projectileType, this::resolveHandlers);
    }

    private List<Entry<?>> resolveHandlers(Class<?> projectileType) {
        List<Entry<?>> resolved = new ArrayList<>();
        for (Entry<?> entry : handlers) {
            if (entry.supports(projectileType)) {
                resolved.add(entry);
            }
        }
        return List.copyOf(resolved);
    }

    private record Entry<P extends Projectile>(Class<P> projectileType, PiProjectileLifecycleHandler<? super P> handler) {
        private void onJoinLevel(Projectile projectile, PiProjectileLifecycleContext context) {
            P cast = cast(projectile);
            if (cast != null) {
                handler.onJoinLevel(cast, context);
            }
        }

        private boolean supports(Class<?> resolvedType) {
            return projectileType.isAssignableFrom(resolvedType);
        }

        private PiProjectileLifecycleHandler<?> rawHandler() {
            return handler;
        }

        private P cast(Projectile projectile) {
            return projectileType.isInstance(projectile) ? projectileType.cast(projectile) : null;
        }
    }
}
