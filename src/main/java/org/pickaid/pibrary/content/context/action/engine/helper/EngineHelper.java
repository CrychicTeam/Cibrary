package org.pickaid.pibrary.content.context.action.engine.helper;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.pickaid.pibrary.content.context.action.engine.context.BuilderContext;
import org.pickaid.pibrary.content.context.action.engine.core.Verifiable;
import net.minecraftforge.registries.IForgeRegistry;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class EngineHelper {
	private static final Map<Class<?>, EngineHelper> CACHE = new ConcurrentHashMap<>();

	private static final Map<Class<?>, Boolean> VERIFIABLE_CACHE = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Boolean> LIST_CACHE = new ConcurrentHashMap<>();

	public static EngineHelper get(Class<?> cls) {
		return CACHE.computeIfAbsent(cls, EngineHelper::of);
	}

	public static void verifyFields(Verifiable obj, BuilderContext ctx, Class<?> cls) {
		validateRecordClass(cls);

		try {
			Set<String> verificationParams = obj.verificationParameters();
			EngineHelper helper = get(cls);

			verifyVerifiableFields(obj, ctx, verificationParams, helper);
			verifyCollectionFields(obj, ctx, verificationParams, helper);

		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to verify fields for class " + cls.getSimpleName(), e);
		} catch (Exception e) {
			throw new IllegalStateException("Configuration verification failed for class " + cls.getSimpleName(), e);
		}
	}

	private static void verifyVerifiableFields(Verifiable obj, BuilderContext ctx,
											   Set<String> verificationParams, EngineHelper helper)
			throws ReflectiveOperationException {

		for (Method accessor : helper.verifiableAccessors) {
			Object fieldValue = accessor.invoke(obj);
			if (fieldValue instanceof Verifiable verifiable) {
				String fieldName = accessor.getName();
				verifiable.verify(ctx.of(fieldName, verificationParams));
			}
		}
	}

	private static void verifyCollectionFields(Verifiable obj, BuilderContext ctx,
											   Set<String> verificationParams, EngineHelper helper)
			throws ReflectiveOperationException {

		for (CollectionAccessor collectionAccessor : helper.collectionAccessors) {
			Object fieldValue = collectionAccessor.accessor.invoke(obj);
			if (fieldValue == null) continue;

			String fieldName = collectionAccessor.accessor.getName();

			if (fieldValue instanceof List<?> list) {
				verifyList(list, ctx, verificationParams, fieldName);
			} else if (fieldValue instanceof Collection<?> collection) {
				verifyCollection(collection, ctx, verificationParams, fieldName);
			}
		}
	}

	private static void verifyList(List<?> list, BuilderContext ctx, Set<String> verificationParams, String fieldName) {
		for (int i = 0; i < list.size(); i++) {
			Object item = list.get(i);
			if (item instanceof Verifiable verifiable) {
				verifiable.verify(ctx.of(fieldName + "[" + i + "]", verificationParams));
			}
		}
	}

	private static void verifyCollection(Collection<?> collection, BuilderContext ctx,
										 Set<String> verificationParams, String fieldName) {
		int index = 0;
		for (Object item : collection) {
			if (item instanceof Verifiable verifiable) {
				verifiable.verify(ctx.of(fieldName + "[" + index + "]", verificationParams));
			}
			index++;
		}
	}

	private static void validateRecordClass(Class<?> cls) {
		if (!cls.isRecord()) {
			throw new IllegalArgumentException("Class " + cls.getSimpleName() + " must be a record");
		}
	}

	private static EngineHelper of(Class<?> cls) {
		try {
			return new EngineHelper(cls);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to create EngineHelper for class " + cls.getSimpleName(), e);
		}
	}

	private static class CollectionAccessor {
		final Method accessor;
		final Class<?> elementType;

		CollectionAccessor(Method accessor, Class<?> elementType) {
			this.accessor = accessor;
			this.elementType = elementType;
		}
	}

	private final List<Method> verifiableAccessors = new ArrayList<>();
	private final List<CollectionAccessor> collectionAccessors = new ArrayList<>();

	private EngineHelper(Class<?> cls) {
		validateRecordClass(cls);
		processRecordComponents(cls);
	}

	private void processRecordComponents(Class<?> cls) {
		RecordComponent[] components = cls.getRecordComponents();
		if (components == null || components.length == 0) {
			return;
		}

		for (RecordComponent component : components) {
			try {
				processComponent(component);
			} catch (Exception e) {
				throw new IllegalStateException(
						"Failed to process component '" + component.getName() +
								"' in record " + cls.getSimpleName(), e);
			}
		}
	}

	private void processComponent(RecordComponent component) {
		Method accessor = component.getAccessor();
		Class<?> fieldType = component.getType();
		Type genericType = component.getGenericType();
		if (isVerifiableType(fieldType)) {
			verifiableAccessors.add(accessor);
		}
		if (isCollectionType(fieldType)) {
			Class<?> elementType = extractCollectionElementType(genericType);
			collectionAccessors.add(new CollectionAccessor(accessor, elementType));
		}
	}

	private boolean isVerifiableType(Class<?> type) {
		return VERIFIABLE_CACHE.computeIfAbsent(type,
                Verifiable.class::isAssignableFrom);
	}

	private boolean isCollectionType(Class<?> type) {
		return LIST_CACHE.computeIfAbsent(type,
                Collection.class::isAssignableFrom);
	}

	private Class<?> extractCollectionElementType(Type genericType) {
		if (genericType instanceof ParameterizedType paramType) {
			Type[] typeArgs = paramType.getActualTypeArguments();
			if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> elementClass) {
				return elementClass;
			}
		}
		return Object.class;
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

	public static void clearCache() {
		CACHE.clear();
		VERIFIABLE_CACHE.clear();
		LIST_CACHE.clear();
	}

	public static Map<String, Integer> getCacheStats() {
		Map<String, Integer> stats = new HashMap<>();
		stats.put("helperCache", CACHE.size());
		stats.put("verifiableCache", VERIFIABLE_CACHE.size());
		stats.put("listCache", LIST_CACHE.size());
		return Collections.unmodifiableMap(stats);
	}
}