package org.pickaid.pibrary.content.context.action.engine.helper;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.pickaid.pibrary.Pibrary;

import java.util.function.Supplier;

public record EngineRegistryInstance<T>(ResourceKey<Registry<T>> key, Supplier<IForgeRegistry<T>> registry) {

	public static <T> EngineRegistryInstance<T> of(String id) {
		ResourceKey<Registry<T>> key = Pibrary.REG.create(id);
		return new EngineRegistryInstance<T>(key, Pibrary.REG.make(key).makeRegistry(RegistryBuilder::new));
	}
	public Codec<T> codec() {
		return EngineHelper.lazyCodec(registry);
	}
}
