package org.pickaid.pibrary.api.targeting;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.api.core.PibraryScopeKey;
import org.pickaid.pibrary.api.core.PibraryScopes;

/**
 * Global accessor for the installed {@link PiTargetingResolver}.
 */
public final class PiTargeting {
    public static final PibraryScopeKey<PiTargetingResolver> KEY =
            new PibraryScopeKey<>(Pibrary.id("targeting_resolver"), PiTargetingResolver.class);

    private PiTargeting() {
    }

    /**
     * Installs the global targeting resolver.
     *
     * @param resolver resolver instance or {@code null} to remove
     */
    public static void install(PiTargetingResolver resolver) {
        PibraryScopes.install(KEY, resolver);
    }

    /**
     * Finds the global targeting resolver.
     *
     * @return installed resolver, if present
     */
    public static Optional<PiTargetingResolver> find() {
        return PibraryScopes.findGlobal(KEY);
    }

    /**
     * Requires the global targeting resolver.
     *
     * @return installed resolver
     */
    public static PiTargetingResolver require() {
        return PibraryScopes.requireGlobal(KEY);
    }

    /**
     * Resolves a target query using the installed resolver.
     *
     * @param caster casting entity
     * @param query immutable target query
     * @return immutable ordered list of matching entities
     */
    public static List<Entity> resolve(Entity caster, PiTargetQuery query) {
        return require().resolve(Objects.requireNonNull(caster, "caster"), Objects.requireNonNull(query, "query"));
    }
}
