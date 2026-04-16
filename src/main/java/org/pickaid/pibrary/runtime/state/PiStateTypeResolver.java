package org.pickaid.pibrary.runtime.state;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import org.pickaid.pibrary.api.service.PiStateLivingEntityService;

public final class PiStateTypeResolver {
    private static final ClassValue<Class<?>> LIVING_SERVICE_STATE_TYPES = new ClassValue<>() {
        @Override
        protected Class<?> computeValue(Class<?> type) {
            return resolveTypeArgument(type, PiStateLivingEntityService.class, 0);
        }
    };

    private PiStateTypeResolver() {
    }

    @SuppressWarnings("unchecked")
    public static <S> Class<S> livingServiceStateType(Class<? extends PiStateLivingEntityService<S>> serviceType) {
        return (Class<S>) LIVING_SERVICE_STATE_TYPES.get(serviceType);
    }

    private static Class<?> resolveTypeArgument(Class<?> leafType, Class<?> targetBase, int index) {
        Class<?> current = leafType;
        Map<TypeVariable<?>, Type> bindings = new HashMap<>();
        while (current != null && current != Object.class) {
            Type generic = current.getGenericSuperclass();
            if (generic instanceof ParameterizedType parameterized
                    && parameterized.getRawType() instanceof Class<?> rawClass) {
                TypeVariable<?>[] variables = rawClass.getTypeParameters();
                Type[] arguments = parameterized.getActualTypeArguments();
                for (int i = 0; i < variables.length && i < arguments.length; i++) {
                    bindings.put(variables[i], substitute(arguments[i], bindings));
                }
                if (rawClass == targetBase) {
                    Type actual = substitute(parameterized.getActualTypeArguments()[index], bindings);
                    if (actual instanceof Class<?> actualClass) {
                        return actualClass;
                    }
                    if (actual instanceof ParameterizedType actualParameterized
                            && actualParameterized.getRawType() instanceof Class<?> rawActual) {
                        return rawActual;
                    }
                    throw new IllegalStateException("Unsupported Pi state type for " + leafType.getName() + ": " + actual);
                }
                current = rawClass;
                continue;
            }
            if (generic instanceof Class<?> rawClass) {
                current = rawClass;
                continue;
            }
            break;
        }
        throw new IllegalStateException("Could not resolve Pi state type for " + leafType.getName());
    }

    private static Type substitute(Type type, Map<TypeVariable<?>, Type> bindings) {
        Type current = type;
        while (current instanceof TypeVariable<?> variable && bindings.containsKey(variable)) {
            current = bindings.get(variable);
        }
        return current;
    }
}
