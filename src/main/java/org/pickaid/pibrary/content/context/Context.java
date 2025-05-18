package org.pickaid.pibrary.content.context;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.pickaid.pibrary.content.context.params.ContextParam;
import org.pickaid.pibrary.content.context.params.ContextParamSet;
import org.pickaid.pibrary.content.context.params.ContextParams;

import java.util.NoSuchElementException;
import java.util.Optional;

public class Context {
	private final RandomSource random;
	private final ContextParams params;
	
	public Context(RandomSource random, ContextParams params) {
		this.random = random;
		this.params = params;
	}
	
	public static Context createLocationContext(ServerLevel level, BlockPos checkPos) {
		return builder(ContextParams.builder(level).withParameter(ContextParam.LOCATION, checkPos.getCenter()).build(ContextParamSet.LOCATION)).build();
	}
	
	public RandomSource getRandom() {
		return random;
	}
	
	public ServerLevel getLevel() {
		return params.getLevel();
	}
	
	public boolean hasParam(ContextParam<?> param) {
		return params.hasParam(param);
	}
	
	/**
	 * 获取上下文参数的对象, 仅用于测试
	 *
	 * @param param 上下文参数
	 * @param <T>   上下文参数的数据类型
	 * @return 上下文参数对象
	 * @throws NoSuchElementException 找不到上下文参数
	 */
	public <T> T getParameter(ContextParam<T> param) {
		if (!hasParam(param)) {
			throw new NoSuchElementException(param.name().toString());
		}
		return params.getParameter(param);
	}
	
	/**
	 * 获取可选的上下文参数, 建议使用
	 *
	 * @param param 上下文参数
	 * @param <T>   可选的实际参数对象
	 * @return 可选的上下文参数对象
	 */
	public <T> Optional<T> getOptionalParameter(ContextParam<T> param) {
		return params.getOptionalParameter(param);
	}
	
	/**
	 * 获取上下文参数, 可能为Null, 建议使用
	 *
	 * @param param 上下文参数
	 * @param <T>   上下文参数类型
	 * @return 实际参数对象
	 */
	public @Nullable <T> T getParameterOrNull(ContextParam<T> param) {
		return params.getParameterOrNull(param);
	}
	
	/**
	 * 从上下文创建构造器
	 *
	 * @param context Context
	 * @return Context.Builder
	 */
	public static Builder builder(Context context) {
		return new Builder(context);
	}
	
	/**
	 * 提供上下文参数以构造上下文, 可使用{@link ContextParams#builder(ServerLevel)}
	 *
	 * @param params 上下文参数
	 * @return Builder
	 */
	public static Builder builder(ContextParams params) {
		return new Builder(params);
	}
	
	public static class Builder {
		private final ContextParams params;
		private RandomSource random;
		private ResourceLocation id;
		
		private Builder(ContextParams params) {
			this.params = params;
		}
		
		private Builder(Context context) {
			this.params = context.params;
			this.random = context.random;
		}
		
		public Builder withOptionalRandomSeed(long seed) {
			random = RandomSource.create(seed);
			return this;
		}
		
		public Builder withQueriedLootTableId(ResourceLocation id) {
			this.id = id;
			return this;
		}
		
		public ServerLevel getLevel() {
			return params.getLevel();
		}
		
		/**
		 * 构造, 相当于{@link Builder#build(ResourceLocation)} 传递null值
		 *
		 * @return Context
		 */
		public Context build() {
			return build(null);
		}
		
		/**
		 * 该build使用ResourceLocation创建RandomSource
		 *
		 * @param id ResourceLocation-用于{@link ServerLevel#getRandomSequence(ResourceLocation)}
		 * @return Context
		 */
		public Context build(@Nullable ResourceLocation id) {
			if (this.random != null) {
				return new Context(random, params);
			}
			
			if (id == null) {
				if (this.id != null) {
					this.random = getLevel().getRandomSequence(this.id);
				} else {
					this.random = RandomSource.create();
				}
			} else {
				this.random = getLevel().getRandomSequence(id);
			}
			
			return new Context(random, params);
		}
	}
}
