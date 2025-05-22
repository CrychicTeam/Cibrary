package org.pickaid.pibrary.api.fastprojectileapi.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.pickaid.pibrary.api.fastprojectileapi.collision.LaserHitHelper;

public abstract class BaseLaser extends SimplifiedProjectile {

	public BaseLaser(EntityType<?> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	public abstract double getLength();

	public abstract boolean checkBlockHit();

	public abstract boolean checkEntityHit();

	public abstract float getEffectiveHitRadius();

	public void tick() {
		super.tick();
		var hit = LaserHitHelper.getHitResultOnProjection(this, checkBlockHit(), checkEntityHit());
		onHit(hit);
	}

	protected void onHit(LaserHitHelper.LaserHitResult hit) {

	}

	@Override
	protected void defineSynchedData() {

	}

}
