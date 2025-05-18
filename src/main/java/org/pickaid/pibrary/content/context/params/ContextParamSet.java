package org.pickaid.pibrary.content.context.params;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.minecraft.resources.ResourceLocation;
import org.pickaid.pibrary.Pibrary;;

import java.util.Set;
import java.util.function.Function;

public final class ContextParamSet {
	public static final ContextParamSet LOCATION = create("location", builder -> builder.required(ContextParam.LOCATION).build());

	private final ResourceLocation name;
	private final Set<ContextParam<?>> required;
	private final Set<ContextParam<?>> allowed;
	
	public ContextParamSet(ResourceLocation name, Set<ContextParam<?>> required, Set<ContextParam<?>> optional) {
		this.name = name;
		this.required = ImmutableSet.copyOf(required);
		this.allowed = ImmutableSet.copyOf(Sets.union(required, optional));
	}
	
	private static ContextParamSet create(String id, Function<Builder, ContextParamSet> function) {
		return create(Pibrary.source(id), function);
	}
	
	/**
	 * 用于创建上下文参数集(也称为上下文类型)
	 *
	 * @param id       标识符ResourceLocation
	 * @param function 提供Builder 返回参数集ContextParamSet
	 * @return 参数集ContextParamSet
	 */
	public static ContextParamSet create(ResourceLocation id, Function<Builder, ContextParamSet> function) {
		return function.apply(builder(id));
	}
	
	public ResourceLocation getName() {
		return name;
	}
	
	public Set<ContextParam<?>> getAllowed() {
		return allowed;
	}
	
	public Set<ContextParam<?>> getRequired() {
		return required;
	}
	
	/**
	 * 上下文参数集构造器
	 *
	 * @param name 标识符ResourceLocation
	 * @return ContextParamSet.Builder
	 */
	public static Builder builder(ResourceLocation name) {
		return new Builder(name);
	}
	
	public static class Builder {
		private final ResourceLocation name;
		private final Set<ContextParam<?>> required = Sets.newHashSet();
		private final Set<ContextParam<?>> optional = Sets.newHashSet();
		
		private Builder(ResourceLocation name) {
			this.name = name;
		}
		
		public Builder required(ContextParam<?> param) {
			if (optional.contains(param)) {
				throw new IllegalArgumentException("Parameter " + param.name() + " is already optional");
			} else {
				required.add(param);
				return this;
			}
		}
		
		public Builder optional(ContextParam<?> param) {
			if (required.contains(param)) {
				throw new IllegalArgumentException("Parameter " + param.name() + " is already required");
			} else {
				optional.add(param);
				return this;
			}
		}
		
		public ContextParamSet build() {
			return new ContextParamSet(name, required, optional);
		}
	}
	
	
}
