package org.pickaid.pibrary.content.context.action.engine.helper;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;
import org.pickaid.pibrary.content.context.action.engine.core.Verifiable;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class EngineHelper {

	private static final Map<Class<?>, EngineHelper> CACHE = new LinkedHashMap<>();

	public static EngineHelper get(Class<?> cls) {
		if (CACHE.containsKey(cls)) {
			return CACHE.get(cls);
		}
		var ans = of(cls);
		CACHE.put(cls, ans);
		return ans;
	}

	public static void verifyFields(Verifiable obj, BuilderContext ctx, Class<?> cls) {
		if (!cls.isRecord())
			throw new IllegalStateException("class " + cls.getSimpleName() + " is not a record");
		try {
			var set = obj.verificationParameters();
			var helper = get(cls);

			// 使用访问器方法而不是直接访问字段
			for (var accessor : helper.childAccessors) {
				Verifiable v = (Verifiable) accessor.invoke(obj);
				if (v != null) {
					String fieldName = accessor.getName();
					v.verify(ctx.of(fieldName, set));
				}
			}

			for (var accessor : helper.collectionAccessors) {
				List l = (List) accessor.invoke(obj);
				if (l != null) {
					String fieldName = accessor.getName();
					for (int i = 0; i < l.size(); i++) {
						if (l.get(i) instanceof Verifiable v)
							v.verify(ctx.of(fieldName + "[" + i + "]", set));
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("class " + cls.getSimpleName() + " failed configuration", e);
		}
	}

	private static EngineHelper of(Class<?> cls) {
		try {
			return new EngineHelper(cls);
		} catch (Exception e) {
			throw new IllegalStateException("class " + cls.getSimpleName() + " failed configuration", e);
		}
	}

	private final List<Method> childAccessors = new ArrayList<>();
	private final List<Method> collectionAccessors = new ArrayList<>();

	public EngineHelper(Class<?> cls) throws Exception {
		if (!cls.isRecord()) {
			throw new IllegalStateException("Class " + cls.getSimpleName() + " is not a record");
		}

		RecordComponent[] components = cls.getRecordComponents();
		if (components == null || components.length == 0) {
			// 空记录也是有效的，只是没有组件需要验证
			return;
		}

		for (RecordComponent component : components) {
			try {
				// 使用记录组件的访问器方法，而不是直接访问字段
				Method accessor = component.getAccessor();
				Class<?> fieldType = component.getType();

				if (Verifiable.class.isAssignableFrom(fieldType)) {
					childAccessors.add(accessor);
				}

				if (List.class.isAssignableFrom(fieldType)) {
					collectionAccessors.add(accessor);
				}
			} catch (Exception e) {
				throw new IllegalStateException("Failed to process component " + component.getName() + " in class " + cls.getSimpleName(), e);
			}
		}
	}

	public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> cls, T[] vals) {
		return Codec.STRING.xmap(e -> {
			try {
				return Enum.valueOf(cls, e);
			} catch (Exception ex) {
				throw new IllegalArgumentException(e + " is not a valid " + cls.getSimpleName() + ". Valid values are: " + List.of(vals));
			}
		}, Enum::name);
	}

	public static <T> Codec<T> lazyCodec(Supplier<IForgeRegistry<T>> registry) {
		return new Codec<>() {
			@Override
			public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> a, T1 b) {
				return registry.get().getCodec().decode(a, b);
			}

			@Override
			public <T1> DataResult<T1> encode(T a, DynamicOps<T1> b, T1 c) {
				return registry.get().getCodec().encode(a, b, c);
			}
		};
	}
}