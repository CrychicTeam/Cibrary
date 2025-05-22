package org.pickaid.pibrary.content.context.action.particle.core;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.TextureSheetParticle;
import org.jetbrains.annotations.Nullable;

public class PiGenericParticleProvider implements ParticleProvider<PiGenericParticleOption> {

	@Nullable
	@Override
	public TextureSheetParticle createParticle(
			PiGenericParticleOption option, ClientLevel level,
			double x, double y, double z, double vx, double vy, double vz) {
		return new PiGenericParticle(level, x, y, z, vx, vy, vz, option.data());
	}

}
