package org.pickaid.pibrary.content.context.action.particle.render;

import org.pickaid.pibrary.api.fastprojectileapi.entity.ProjectileMovement;
import org.pickaid.pibrary.content.context.action.particle.core.PiGenericParticle;
import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record OrientedSpriteRenderer(
		ParticleRenderer inner,
		Vec3 facing,
		Int2DoubleFunction roll
) implements OrientableSpriteRenderer {

	@Override
	public void onParticleInit(PiGenericParticle e) {
		inner.onParticleInit(e);
		setRot(e);
		var ori = e.getOrientation();
		ori.preTick(ori.getX(1), ori.getY(1), ori.getZ(1));
	}

	@Override
	public void onPostTick(PiGenericParticle e) {
		inner.onPostTick(e);
		setRot(e);
	}

	private void setRot(PiGenericParticle e) {
		var ori = e.getOrientation();
		double rz = roll.get(e.age()) * Mth.DEG_TO_RAD;
		if (facing().lengthSqr() == 0) {
			ori.setRot(ori.getRX(1), ori.getRY(1), ori.getRZ(1) + rz);
		} else {
			var rot = ProjectileMovement.of(facing).rot();
			ori.setRot(rot.x, rot.y, rot.z + rz);
		}
		if (e.age() == 1) ori.preTick(ori.getX(1), ori.getY(1), ori.getZ(1));
	}

}
