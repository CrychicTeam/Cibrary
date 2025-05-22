package org.pickaid.pibrary.content.context.action.particle.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class PiGenericParticleType extends ParticleType<PiGenericParticleOption> {

	public PiGenericParticleType() {
		super(true, new PiGenericParticleDeserializer());
	}

	@Override
	public Codec<PiGenericParticleOption> codec() {
		return PiGenericParticleOption.CODEC;
	}

}
