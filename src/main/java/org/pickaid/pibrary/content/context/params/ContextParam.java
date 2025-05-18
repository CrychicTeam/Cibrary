package org.pickaid.pibrary.content.context.params;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.pickaid.pibrary.Pibrary;;

/**
 * A context parameter is a key-value pair that can be used to store and retrieve
 *
 * @param name id
 * @param <T>  Generic type.
 */
public record ContextParam<T>(ResourceLocation name) {
	public static final ContextParam<Vec3> LOCATION = create("location");
	
	private static <T> ContextParam<T> create(String id) {
		return create(Pibrary.source(id));
	}

	/**
	 * Creates a context parameter with the specified resource location identifier.
	 * This method instantiates a new ContextParam object that can be used to store
	 * and retrieve typed values in a context.
	 *
	 * @param id  The ResourceLocation that uniquely identifies this parameter
	 * @param <T> The generic type parameter representing the data type this parameter will hold
	 * @return A new ContextParam instance with the specified identifier
	 */
	public static <T> ContextParam<T> create(ResourceLocation id) {
		return new ContextParam<>(id);
	}
	
	@Override
	public @NotNull String toString() {
		return "<parameter" + name + ">";
	}
}
