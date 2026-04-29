package org.pickaid.pibrary.runtime.targeting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.api.entity.PiEntitySpatialIndex;
import org.pickaid.pibrary.api.targeting.PiTargetAnchor;
import org.pickaid.pibrary.api.targeting.PiTargetQuery;
import org.pickaid.pibrary.api.targeting.PiTargetingResolver;

/**
 * Default targeting runtime that resolves target volumes and queries the installed
 * entity spatial index.
 */
public final class PiDefaultTargetingResolver implements PiTargetingResolver {
    private final PiEntitySpatialIndex spatialIndex;

    /**
     * Creates the default targeting runtime.
     *
     * @param spatialIndex spatial index used for candidate entity queries
     */
    public PiDefaultTargetingResolver(PiEntitySpatialIndex spatialIndex) {
        this.spatialIndex = Objects.requireNonNull(spatialIndex, "spatialIndex");
    }

    @Override
    public List<Entity> resolve(Entity caster, PiTargetQuery query) {
        Objects.requireNonNull(caster, "caster");
        Objects.requireNonNull(query, "query");

        ResolvedTargetVolume volume = resolveVolume(
                caster.getBoundingBox().getCenter(),
                caster.getEyePosition(),
                caster.getViewVector(1.0F),
                query
        );
        List<TargetHit> resolved = new ArrayList<>();
        for (Entity candidate : spatialIndex.query(caster.level(), volume.queryBounds(), entity -> accepts(caster, query, volume, entity))) {
            resolved.add(new TargetHit(candidate, distanceSqr(candidate.getBoundingBox(), volume)));
        }
        resolved.sort(Comparator.comparingDouble(TargetHit::distanceSqr));

        List<Entity> entities = new ArrayList<>(resolved.size());
        for (TargetHit hit : resolved) {
            entities.add(hit.entity());
        }
        return List.copyOf(entities);
    }

    /**
     * Resolves an abstract target query into a concrete query volume. Exposed for focused tests.
     *
     * @param casterCenter center point of the caster
     * @param casterEyePosition eye position of the caster
     * @param casterViewVector caster look vector
     * @param query immutable target query
     * @return resolved target volume
     */
    static ResolvedTargetVolume resolveVolume(Vec3 casterCenter, Vec3 casterEyePosition, Vec3 casterViewVector, PiTargetQuery query) {
        Objects.requireNonNull(casterCenter, "casterCenter");
        Objects.requireNonNull(casterEyePosition, "casterEyePosition");
        Objects.requireNonNull(casterViewVector, "casterViewVector");
        Objects.requireNonNull(query, "query");

        return switch (query.anchor()) {
            case SELF -> sphere(query.anchor(), casterCenter, query.radius());
            case LOOK_VECTOR -> {
                Vec3 end = casterEyePosition.add(casterViewVector.normalize().scale(query.range()));
                yield sweep(query.anchor(), casterEyePosition, end, query.radius());
            }
            case PROJECTED_POINT -> {
                Vec3 center = query.anchorPoint().add(query.anchorDirection().scale(query.range()));
                yield sphere(query.anchor(), center, query.radius());
            }
            case AREA_CENTER -> sphere(query.anchor(), query.anchorPoint(), query.radius());
        };
    }

    /**
     * Returns whether the given bounds intersect the resolved target volume. Exposed for focused tests.
     *
     * @param bounds entity bounds
     * @param volume resolved target volume
     * @return {@code true} when the bounds are inside the query volume
     */
    static boolean matches(AABB bounds, ResolvedTargetVolume volume) {
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(volume, "volume");

        return switch (volume.anchor()) {
            case LOOK_VECTOR -> intersectsSweep(bounds, volume.start(), volume.end(), volume.radius());
            case SELF, PROJECTED_POINT, AREA_CENTER -> bounds.distanceToSqr(volume.center()) <= volume.radius() * volume.radius();
        };
    }

    /**
     * Computes a stable sorting distance between candidate bounds and the resolved target volume.
     * Exposed for focused tests.
     *
     * @param bounds entity bounds
     * @param volume resolved target volume
     * @return squared distance used for ordering
     */
    static double distanceSqr(AABB bounds, ResolvedTargetVolume volume) {
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(volume, "volume");

        return switch (volume.anchor()) {
            case LOOK_VECTOR -> {
                AABB inflated = bounds.inflate(volume.radius());
                Optional<Vec3> clip = inflated.clip(volume.start(), volume.end());
                if (clip.isPresent()) {
                    yield volume.start().distanceToSqr(clip.get());
                }
                if (inflated.contains(volume.start()) || inflated.contains(volume.end())) {
                    yield 0.0D;
                }
                yield inflated.distanceToSqr(volume.start());
            }
            case SELF, PROJECTED_POINT, AREA_CENTER -> bounds.distanceToSqr(volume.center());
        };
    }

    private boolean accepts(Entity caster, PiTargetQuery query, ResolvedTargetVolume volume, Entity candidate) {
        if (candidate == caster) {
            return query.includeCaster() && matches(candidate.getBoundingBox(), volume);
        }
        if (!candidate.isAlive() || candidate.isSpectator() || !candidate.isPickable()) {
            return false;
        }
        if (query.livingOnly() && !(candidate instanceof LivingEntity)) {
            return false;
        }
        if (!matches(candidate.getBoundingBox(), volume)) {
            return false;
        }
        return !query.requireLineOfSight() || hasLineOfSight(caster, candidate);
    }

    private boolean hasLineOfSight(Entity caster, Entity candidate) {
        HitResult hit = caster.level().clip(new ClipContext(
                caster.getEyePosition(),
                candidate.getEyePosition(),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                caster
        ));
        return hit == null || hit.getType() == HitResult.Type.MISS;
    }

    private static boolean intersectsSweep(AABB bounds, Vec3 start, Vec3 end, double radius) {
        AABB inflated = bounds.inflate(radius);
        return inflated.clip(start, end).isPresent() || inflated.contains(start) || inflated.contains(end);
    }

    private static ResolvedTargetVolume sphere(PiTargetAnchor anchor, Vec3 center, double radius) {
        return new ResolvedTargetVolume(anchor, null, null, center, radius, new AABB(center, center).inflate(radius));
    }

    private static ResolvedTargetVolume sweep(PiTargetAnchor anchor, Vec3 start, Vec3 end, double radius) {
        return new ResolvedTargetVolume(anchor, start, end, null, radius, new AABB(start, end).inflate(radius));
    }

    private record TargetHit(Entity entity, double distanceSqr) {
    }

    /**
     * Concrete resolved query volume used internally by the default targeting runtime.
     */
    static final class ResolvedTargetVolume {
        private final PiTargetAnchor anchor;
        private final Vec3 start;
        private final Vec3 end;
        private final Vec3 center;
        private final double radius;
        private final AABB queryBounds;

        private ResolvedTargetVolume(PiTargetAnchor anchor, Vec3 start, Vec3 end, Vec3 center, double radius, AABB queryBounds) {
            this.anchor = Objects.requireNonNull(anchor, "anchor");
            this.start = start;
            this.end = end;
            this.center = center;
            this.radius = radius;
            this.queryBounds = Objects.requireNonNull(queryBounds, "queryBounds");
        }

        PiTargetAnchor anchor() {
            return anchor;
        }

        Vec3 start() {
            return start;
        }

        Vec3 end() {
            return end;
        }

        Vec3 center() {
            return center;
        }

        double radius() {
            return radius;
        }

        AABB queryBounds() {
            return queryBounds;
        }
    }
}
