package org.pickaid.pibrary.content.context.action.particle.core;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;

public class PiGenericParticleDeserializer implements ParticleOptions.Deserializer<PiGenericParticleOption> {

	@Override
	public PiGenericParticleOption fromCommand(ParticleType<PiGenericParticleOption> type, StringReader reader) throws CommandSyntaxException {
		return new PiGenericParticleOption();
	}

	@Override
	public PiGenericParticleOption fromNetwork(ParticleType<PiGenericParticleOption> type, FriendlyByteBuf buf) {
		return new PiGenericParticleOption();
	}

}
