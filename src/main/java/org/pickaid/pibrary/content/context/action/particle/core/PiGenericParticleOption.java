package org.pickaid.pibrary.content.context.action.particle.core;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.init.LibraryRegistries;

public class PiGenericParticleOption implements ParticleOptions {

	public static final Codec<PiGenericParticleOption> CODEC = Codec.unit(PiGenericParticleOption::new);

	@Nullable
	private final ClientParticleData data;

	public PiGenericParticleOption(ClientParticleData data) {
		this.data = data;
	}

	public PiGenericParticleOption() {
		this.data = null;
	}

	@Override
	public @NotNull ParticleType<?> getType() {
		return LibraryRegistries.GENERIC_PARTICLE.get();
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buf) {
	}

	@Override
	public @NotNull String writeToString() {
		var rl = ForgeRegistries.PARTICLE_TYPES.getKey(this.getType());
		assert rl != null;
		return rl.toString();
	}

	@OnlyIn(Dist.CLIENT)
	public PiParticleData data() {
		return data == null ? ClientParticleData.DEFAULT : data;
	}

}
