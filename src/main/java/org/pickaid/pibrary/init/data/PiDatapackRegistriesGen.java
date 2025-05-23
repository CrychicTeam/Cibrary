package org.pickaid.pibrary.init.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import org.jetbrains.annotations.NotNull;
import org.pickaid.pibrary.Pibrary;
import org.pickaid.pibrary.init.LibraryRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class PiDatapackRegistriesGen extends DatapackBuiltinEntriesProvider {


	private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
			.add(LibraryRegistries.ACTION, ctx -> {
				for (var e : SpellDataGenRegistry.LIST) {
					e.register(ctx);
				}
			});

	public PiDatapackRegistriesGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
		super(output, registries, BUILDER, Set.of("minecraft", Pibrary.MODID));
	}

	@NotNull
	public String getName() {
		return "L2Magic Spell Data";
	}

}
