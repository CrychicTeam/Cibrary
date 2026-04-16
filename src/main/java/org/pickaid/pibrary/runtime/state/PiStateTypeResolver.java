package org.pickaid.pibrary.runtime.state;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
        while (current != null && current != Object.class) {
            Type generic = current.getGenericSuperclass();
            if (generic instanceof ParameterizedType parameterized
                    && parameterized.getRawType() instanceof Class<?> rawClass) {
                if (rawClass == targetBase) {
                    Type actual = parameterized.getActualTypeArguments()[index];
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
}
