package org.pickaid.pibrary.content.context.action.engine.helper;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import org.pickaid.pibrary.Pibrary;
import dev.xkmc.l2serial.util.Wrappers;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;

import java.util.function.Supplier;

public record EngineRegistryInstance<T>(ResourceKey<Registry<T>> key, Supplier<IForgeRegistry<T>> registry) {

	public static <T> EngineRegistryInstance<T> of(String id) {
		ResourceKey<Registry<T>> key = Pibrary.REGISTRATE.makeRegistry(id, RegistryBuilder::new);
		return new EngineRegistryInstance<T>(key, Suppliers.memoize(() -> Wrappers.cast(RegistryManager.ACTIVE.getRegistry(key))));
	}

	public Codec<T> codec() {
		return EngineHelper.lazyCodec(registry);
	}

}
