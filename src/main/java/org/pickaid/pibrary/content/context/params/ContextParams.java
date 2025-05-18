package org.pickaid.pibrary.content.context.params;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * 用于Context, 存储参数Key{@link ContextParam}与实际的参数值
 */
public final class ContextParams {
	private final ServerLevel level;
	private final ContextParamSet paramSet;
	private final Map<ContextParam<?>, Object> params;
	
	public ContextParams(ServerLevel level, ContextParamSet paramSet, Map<ContextParam<?>, Object> params) {
		this.level = level;
		this.paramSet = paramSet;
		this.params = params;
	}
	
	public ServerLevel getLevel() {
		return level;
	}
	
	public ContextParamSet getParamSet() {
		return paramSet;
	}
	
	public boolean hasParam(ContextParam<?> param) {
		return params.containsKey(param);
	}
	
	public <T> T getParameter(ContextParam<T> param) {
		if (params.containsKey(param)) {
			return (T) params.get(param);
		} else {
			throw new NoSuchElementException(param.name().toString());
		}
	}
	
	public <T> Optional<T> getOptionalParameter(ContextParam<T> param) {
		return Optional.of((T) params.get(param));
	}
	
	public <T> T getParameterOrNull(ContextParam<T> param) {
		return (T) params.get(param);
	}
	
	public static Builder builder(ServerLevel level) {
		return new Builder(level);
	}
	
	/**
	 * Context params Builder
	 */
	public static class Builder {
		private final ServerLevel level;
		private final Map<ContextParam<?>, Object> params = Maps.newHashMap();
		
		private Builder(ServerLevel level) {
			this.level = level;
		}
		
		public <T> Builder withParameter(ContextParam<T> param, T value) {
			params.put(param, value);
			return this;
		}
		
		public <T> Builder withOptionalParameter(ContextParam<T> param, @Nullable T value) {
			if (value == null) {
				params.remove(param);
			} else {
				params.put(param, value);
			}
			return this;
		}
		
		public <T> T getParameter(ContextParam<T> param) {
			if (params.containsKey(param)) {
				return (T) params.get(param);
			} else {
				throw new NoSuchElementException(param.name().toString());
			}
		}
		
		/**
		 * 根据参数集完成上下文参数构造, 这将检查内部的上下文参数是否符合上下文参数集要求, 不满足则抛出错误
		 *
		 * @param paramSet 参数集
		 * @return 上下文参数
		 */
		public ContextParams build(ContextParamSet paramSet) {
			Set<ContextParam<?>> set = Sets.difference(paramSet.getRequired(), params.keySet());
			if (!set.isEmpty()) {
				throw new IllegalArgumentException("Missing required parameters: " + set);
			}
			{
				return new ContextParams(level, paramSet, params);
			}
		}
	}
}
