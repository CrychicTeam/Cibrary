package org.pickaid.pibrary.content.context.action.particle.core;

import dev.xkmc.fastprojectileapi.entity.ProjectileMovement;
import org.pickaid.pibrary.content.context.action.particle.render.ParticleRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface LMParticleData {

	ProjectileMovement move(int age, Vec3 vec3, Vec3 vec31);

	int life();

	float size();

	boolean doCollision();

	@OnlyIn(Dist.CLIENT)
	ParticleRenderer renderer();

}
