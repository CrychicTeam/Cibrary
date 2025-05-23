package org.pickaid.pibrary.content.context.conditions;

import com.mojang.serialization.Codec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.pickaid.pibrary.api.core.Reg;
import org.pickaid.pibrary.init.LibraryRegistries;

;

public record ContextConditionType<T extends ContextPredicate>(Codec<T> codec) {
	private static final DeferredRegister<ContextConditionType<?>> VANILLA_DEFERRED_REGISTER = Reg.of("minecraft").make(LibraryRegistries.PREDICATE_TYPE_KEY);
	public static final RegistryObject<ContextConditionType<LocationCheck>> LOCATION_CHECK = VANILLA_DEFERRED_REGISTER.register("location_check", () -> of(LocationCheck.CODEC));
	public static final RegistryObject<ContextConditionType<AnyOf>> ANY_OF = VANILLA_DEFERRED_REGISTER.register("any_of", () -> of(AnyOf.CODEC));
	public static final RegistryObject<ContextConditionType<AllOf>> ALL_OF = VANILLA_DEFERRED_REGISTER.register("all_of", () -> of(AllOf.CODEC));
	public static final RegistryObject<ContextConditionType<Inverted>> INVERTED = VANILLA_DEFERRED_REGISTER.register("inverted", () -> of(Inverted.CODEC));

	public static <T extends ContextPredicate> ContextConditionType<T> of(Codec<T> codec) {
		return new ContextConditionType<>(codec);
	}
	
	public static void register(IEventBus bus) {
		VANILLA_DEFERRED_REGISTER.register(bus);
	}
}
