package org.pickaid.pibrary.runtime.projectile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.api.entity.PiEntitySpatialIndex;
import org.pickaid.pibrary.api.projectile.PiProjectileCollisionMode;
import org.pickaid.pibrary.api.projectile.PiProjectileTracer;
import org.pickaid.pibrary.api.projectile.PiProjectileTraceRequest;
import org.pickaid.pibrary.api.projectile.PiProjectileTraceResult;

/**
 * Default projectile tracing runtime built on vanilla block clipping and the
 * installed entity spatial index.
 */
public final class PiDefaultProjectileTracer implements PiProjectileTracer {
    private final PiEntitySpatialIndex spatialIndex;

    /**
     * Creates the default tracing runtime.
     *
     * @param spatialIndex spatial index used for candidate entity queries
     */
    public PiDefaultProjectileTracer(PiEntitySpatialIndex spatialIndex) {
        this.spatialIndex = Objects.requireNonNull(spatialIndex, "spatialIndex");
    }

    @Override
    public PiProjectileTraceResult trace(PiProjectileTraceRequest request) {
        Objects.requireNonNull(request, "request");

        Vec3 from = request.from();
        Vec3 to = request.to();
        Vec3 delta = to.subtract(from);
        HitResult blockHit = blockHit(request, from, to, delta);
        Vec3 limit = blockHit.getType() == HitResult.Type.MISS ? to : blockHit.getLocation();

        TraceEntityHits entityHits = collectEntityHits(
                request,
                from,
                limit,
                request.sweepBounds().expandTowards(delta).inflate(1.0D)
        );

        return new PiProjectileTraceResult(
                primaryHit(request.collisionMode(), blockHit, entityHits.directHits(), to, delta),
                entityHits.directHits(),
                entityHits.grazeHits()
        );
    }

    private HitResult blockHit(PiProjectileTraceRequest request, Vec3 from, Vec3 to, Vec3 delta) {
        if (request.collisionMode() == PiProjectileCollisionMode.ENTITY_ONLY) {
            return miss(to, delta);
        }
        HitResult hit = request.level().clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                request.source()
        ));
        return hit == null ? miss(to, delta) : hit;
    }

    private TraceEntityHits collectEntityHits(PiProjectileTraceRequest request, Vec3 from, Vec3 limit, AABB queryBounds) {
        double inflate = Math.max(0.1D, request.sweepBounds().getSize() * 0.25D);
        List<HitCandidate> hits = new ArrayList<>();
        List<HitCandidate> grazes = new ArrayList<>();
        for (Entity entity : spatialIndex.query(request.level(), queryBounds, candidate -> accepts(request, candidate))) {
            AABB hitBox = entity.getBoundingBox().inflate(inflate);
            Optional<Vec3> hit = traceMovingBounds(hitBox, entity.getDeltaMovement(), from, limit, 0.0D);
            if (hit.isPresent()) {
                Vec3 hitLocation = hit.get();
                hits.add(new HitCandidate(entity, hitLocation, from.distanceToSqr(hitLocation)));
            } else if (request.grazeRadius() > 0.0D) {
                traceMovingBounds(hitBox, entity.getDeltaMovement(), from, limit, request.grazeRadius())
                        .ifPresent(grazeLocation ->
                                grazes.add(new HitCandidate(entity, grazeLocation, from.distanceToSqr(grazeLocation))));
            }
        }
        hits.sort(Comparator.comparingDouble(HitCandidate::distanceSqr));
        grazes.sort(Comparator.comparingDouble(HitCandidate::distanceSqr));

        int limitCount = request.collisionMode() == PiProjectileCollisionMode.PIERCE_ENTITIES
                ? request.maxEntityHits()
                : Math.min(1, request.maxEntityHits());
        List<EntityHitResult> resolved = new ArrayList<>(limitCount);
        for (int i = 0; i < hits.size() && i < limitCount; i++) {
            HitCandidate hit = hits.get(i);
            resolved.add(new EntityHitResult(hit.entity(), hit.location()));
        }
        List<EntityHitResult> grazeHits = new ArrayList<>(Math.min(grazes.size(), request.maxEntityHits()));
        for (int i = 0; i < grazes.size() && i < request.maxEntityHits(); i++) {
            HitCandidate graze = grazes.get(i);
            grazeHits.add(new EntityHitResult(graze.entity(), graze.location()));
        }
        return new TraceEntityHits(List.copyOf(resolved), List.copyOf(grazeHits));
    }

    private boolean accepts(PiProjectileTraceRequest request, Entity candidate) {
        if (candidate == request.source()) {
            return request.targetQuery().includeCaster();
        }
        if (!candidate.isAlive() || candidate.isSpectator() || !candidate.isPickable()) {
            return false;
        }
        return !request.targetQuery().livingOnly() || candidate instanceof LivingEntity;
    }

    private HitResult primaryHit(
            PiProjectileCollisionMode mode,
            HitResult blockHit,
            List<EntityHitResult> entityHits,
            Vec3 to,
            Vec3 delta
    ) {
        if (!entityHits.isEmpty()) {
            return entityHits.get(0);
        }
        if (mode != PiProjectileCollisionMode.ENTITY_ONLY && blockHit.getType() != HitResult.Type.MISS) {
            return blockHit;
        }
        return miss(to, delta);
    }

    private BlockHitResult miss(Vec3 to, Vec3 delta) {
        return BlockHitResult.miss(to, direction(delta), BlockPos.containing(to));
    }

    private Direction direction(Vec3 delta) {
        if (delta.lengthSqr() < 1.0E-7D) {
            return Direction.UP;
        }
        return Direction.getNearest((float) delta.x, (float) delta.y, (float) delta.z);
    }

    static Optional<Vec3> traceMovingBounds(AABB bounds, Vec3 targetDelta, Vec3 from, Vec3 to, double inflate) {
        Objects.requireNonNull(bounds, "bounds");
        Objects.requireNonNull(targetDelta, "targetDelta");
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        if (inflate < 0.0D) {
            throw new IllegalArgumentException("inflate must be >= 0");
        }

        AABB inflated = bounds.inflate(inflate);
        int steps = movementSteps(targetDelta);
        Vec3 nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int i = 0; i <= steps; i++) {
            AABB stepBounds = steps == 0 ? inflated : inflated.move(targetDelta.scale((double) i / steps));
            Optional<Vec3> hit = stepBounds.contains(from) ? Optional.of(from) : stepBounds.clip(from, to);
            if (hit.isPresent()) {
                double distance = from.distanceToSqr(hit.get());
                if (distance < nearestDistance) {
                    nearest = hit.get();
                    nearestDistance = distance;
                }
            }
        }
        return Optional.ofNullable(nearest);
    }

    private static int movementSteps(Vec3 targetDelta) {
        double speed = targetDelta.length();
        if (speed < 1.0E-7D) {
            return 0;
        }
        return Math.min(8, Math.max(1, (int) Math.floor(speed / 0.5D)));
    }

    private record HitCandidate(Entity entity, Vec3 location, double distanceSqr) {
    }

    private record TraceEntityHits(List<EntityHitResult> directHits, List<EntityHitResult> grazeHits) {
    }
}
