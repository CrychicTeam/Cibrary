package org.pickaid.pibrary.api.fastprojectileapi.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.pickaid.pibrary.api.fastprojectileapi.collision.ProjectileHitHelper;

public abstract class BaseProjectile extends SimplifiedProjectile {

	protected BaseProjectile(EntityType<? extends BaseProjectile> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Override
	protected void defineSynchedData() {

	}

	public abstract boolean checkBlockHit();

	public abstract int lifetime();

	public void tick() {
		super.tick();
		HitResult hitresult = ProjectileHitHelper.getHitResultOnMoveVector(this, checkBlockHit());
		if (hitresult != null) {
			onHit(hitresult);
		}
		if (tickCount >= lifetime()) {
			if (!level().isClientSide()) {
				discard();
			}
		} else {
			projectileMove();
			if (!level().isClientSide()) {
				var bpos = blockPosition();
				if (!level().hasChunk(bpos.getX() >> 4, bpos.getZ() >> 4)) {
					discard();
				}
			}
		}
	}

	protected void projectileMove() {
		ProjectileMovement movement = updateVelocity(getDeltaMovement(), position());
		setDeltaMovement(movement.vec());
		updateRotation(movement.rot());
		double d2 = getX() + movement.vec().x;
		double d0 = getY() + movement.vec().y;
		double d1 = getZ() + movement.vec().z;
		setPos(d2, d0, d1);
	}

	protected ProjectileMovement updateVelocity(Vec3 vec, Vec3 pos) {
		return ProjectileMovement.of(vec);
	}

	public boolean shouldRenderAtSqrDistance(double pDistance) {
		double d0 = getBoundingBox().getSize() * 4;
		if (Double.isNaN(d0)) d0 = 4;
		d0 *= 64;
		return pDistance < d0 * d0;
	}

	protected void onHit(HitResult hitresult) {
		if (hitresult.getType() == HitResult.Type.MISS) return;
		if (hitresult instanceof EntityHitResult ehit) {
			onHitEntity(ehit);
			level().gameEvent(GameEvent.PROJECTILE_LAND, hitresult.getLocation(), GameEvent.Context.of(this, null));
		} else if (hitresult instanceof BlockHitResult bhit) {
			BlockPos pos = bhit.getBlockPos();
			onHitBlock(bhit);
			level().gameEvent(GameEvent.PROJECTILE_LAND, pos, GameEvent.Context.of(this, level().getBlockState(pos)));
		}
	}

	protected void onHitEntity(EntityHitResult pResult) {
	}

	protected void onHitBlock(BlockHitResult pResult) {
	}

	@Override
	public boolean isValid() {
		return tickCount < lifetime();
	}

}
