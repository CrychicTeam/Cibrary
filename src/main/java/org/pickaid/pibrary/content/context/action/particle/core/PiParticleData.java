package org.pickaid.pibrary.content.context.action.particle.core;

import org.pickaid.pibrary.content.context.action.particle.render.ParticleRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.pickaid.pibrary.api.fastprojectileapi.entity.ProjectileMovement;

public interface PiParticleData {

	ProjectileMovement move(int age, Vec3 vec3, Vec3 vec31);

	int life();

	float size();

	boolean doCollision();

	@OnlyIn(Dist.CLIENT)
	ParticleRenderer renderer();

}
