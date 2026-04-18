package org.pickaid.pibrary.runtime.chunk;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.pickaid.pibrary.api.service.PiChunkService;
import org.pickaid.pibrary.api.service.PiChunkServiceContext;
import org.pickaid.pibrary.api.service.PiStateChunkService;
import org.pickaid.pibrary.runtime.state.PiStateTypeResolver;

public final class PiChunkServiceDescriptors {
    private static final ServiceLoaderRegistry REGISTRY = new ServiceLoaderRegistry();

    private PiChunkServiceDescriptors() {
    }

    public static void bootstrap() {
        REGISTRY.ensureLoaded();
    }

    public static <T extends PiStateChunkService<?>> Optional<PiGeneratedChunkServiceDescriptor<T, ?>> findGenerated(Class<T> serviceType) {
        return REGISTRY.findGenerated(serviceType);
    }

    public static <T extends PiStateChunkService<?>> PiGeneratedChunkServiceDescriptor<T, ?> requireGenerated(Class<T> serviceType) {
        return REGISTRY.requireGenerated(serviceType);
    }

    public static Optional<PiGeneratedChunkServiceDescriptor<?, ?>> findGenerated(ResourceLocation id) {
        return REGISTRY.findGenerated(id);
    }

    private static final class ServiceLoaderRegistry implements PiChunkServiceRegistry {
        private static final Method CAPABILITY_LOOKUP = capabilityLookup();
        private final Map<Class<?>, PiGeneratedChunkServiceDescriptor<?, ?>> byServiceType = new ConcurrentHashMap<>();
        private final Map<ResourceLocation, PiGeneratedChunkServiceDescriptor<?, ?>> byId = new ConcurrentHashMap<>();
        private volatile boolean loaded;

        @Override
        public void register(PiGeneratedChunkServiceDescriptor<?, ?> descriptor) {
            Objects.requireNonNull(descriptor, "descriptor");
            PiGeneratedChunkServiceDescriptor<?, ?> previousByType = byServiceType.putIfAbsent(descriptor.serviceType(), descriptor);
            if (previousByType != null) {
                throw new IllegalStateException("Duplicate Pi chunk service descriptor for " + descriptor.serviceType().getName());
            }
            PiGeneratedChunkServiceDescriptor<?, ?> previousById = byId.putIfAbsent(descriptor.id(), descriptor);
            if (previousById != null) {
                throw new IllegalStateException("Duplicate Pi chunk service descriptor id " + descriptor.id());
            }
        }

        private synchronized void ensureLoaded() {
            if (loaded) {
                return;
            }
            ServiceLoader.load(PiChunkServiceProvider.class).forEach(provider -> provider.register(this));
            loaded = true;
        }

        private Optional<PiGeneratedChunkServiceDescriptor<?, ?>> findGenerated(ResourceLocation id) {
            ensureLoaded();
            return Optional.ofNullable(byId.get(Objects.requireNonNull(id, "id")));
        }

        private <T extends PiStateChunkService<?>> Optional<PiGeneratedChunkServiceDescriptor<T, ?>> findGenerated(Class<T> serviceType) {
            ensureLoaded();
            Class<T> requiredType = Objects.requireNonNull(serviceType, "serviceType");
            PiGeneratedChunkServiceDescriptor<?, ?> descriptor = byServiceType.get(requiredType);
            if (descriptor == null) {
                descriptor = synthesize(requiredType).orElse(null);
                if (descriptor != null) {
                    register(descriptor);
                }
            }
            return descriptor == null ? Optional.empty() : Optional.of(cast(descriptor));
        }

        private <T extends PiStateChunkService<?>> PiGeneratedChunkServiceDescriptor<T, ?> requireGenerated(Class<T> serviceType) {
            return findGenerated(serviceType).orElseThrow(() ->
                    new IllegalStateException("Missing Pi chunk service descriptor for " + serviceType.getName()));
        }

        @SuppressWarnings("unchecked")
        private static <T extends PiStateChunkService<?>> PiGeneratedChunkServiceDescriptor<T, ?> cast(
                PiGeneratedChunkServiceDescriptor<?, ?> descriptor
        ) {
            return (PiGeneratedChunkServiceDescriptor<T, ?>) descriptor;
        }

        private static Method capabilityLookup() {
            try {
                Method method = CapabilityManager.class.getDeclaredMethod("get", String.class, boolean.class);
                method.setAccessible(true);
                return method;
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unable to access Forge capability lookup", exception);
            }
        }

        @SuppressWarnings("unchecked")
        private static <T extends PiStateChunkService<?>> Optional<PiGeneratedChunkServiceDescriptor<T, ?>> synthesize(Class<T> serviceType) {
            if (serviceType.isInterface() || java.lang.reflect.Modifier.isAbstract(serviceType.getModifiers())) {
                return Optional.empty();
            }
            return Optional.of((PiGeneratedChunkServiceDescriptor<T, ?>) new SyntheticDescriptor<>((Class) serviceType));
        }

        private static final class SyntheticDescriptor<T extends PiStateChunkService<S>, S>
                extends PiGeneratedChunkServiceDescriptor<T, S> {
            private final Constructor<T> constructor;
            private final Capability<T> capability;

            @SuppressWarnings("unchecked")
            private SyntheticDescriptor(Class<T> serviceType) {
                super(resolveId(serviceType), serviceType, (Class<S>) PiStateTypeResolver.chunkServiceStateType((Class) serviceType));
                this.constructor = resolveConstructor(serviceType);
                this.capability = resolveCapability(serviceType);
            }

            @Override
            public Capability<T> capability() {
                return capability;
            }

            @Override
            public T create(PiChunkServiceContext context) {
                try {
                    return constructor.newInstance(context);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException exception) {
                    throw new IllegalStateException("Unable to create Pi chunk service " + serviceType().getName(), exception);
                }
            }

            private static <T extends PiStateChunkService<?>> ResourceLocation resolveId(Class<T> serviceType) {
                PiChunkService annotation = serviceType.getAnnotation(PiChunkService.class);
                if (annotation != null) {
                    return ResourceLocation.fromNamespaceAndPath(annotation.namespace(), annotation.path());
                }
                String path = serviceType.getName()
                        .toLowerCase(Locale.ROOT)
                        .replace('.', '/')
                        .replace('$', '/')
                        .replaceAll("[^a-z0-9/._-]", "_");
                return ResourceLocation.fromNamespaceAndPath("pibrary", "generated/" + path);
            }

            private static <T extends PiStateChunkService<?>> Constructor<T> resolveConstructor(Class<T> serviceType) {
                try {
                    Constructor<T> constructor = serviceType.getDeclaredConstructor(PiChunkServiceContext.class);
                    constructor.setAccessible(true);
                    return constructor;
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException(
                            "Pi chunk service " + serviceType.getName() + " must declare a PiChunkServiceContext constructor",
                            exception);
                }
            }

            @SuppressWarnings("unchecked")
            private static <T extends PiStateChunkService<?>> Capability<T> resolveCapability(Class<T> serviceType) {
                try {
                    return (Capability<T>) CAPABILITY_LOOKUP.invoke(
                            CapabilityManager.INSTANCE,
                            serviceType.getName().replace('.', '/'),
                            false);
                } catch (ReflectiveOperationException exception) {
                    throw new IllegalStateException("Unable to resolve capability for Pi chunk service " + serviceType.getName(), exception);
                }
            }
        }
    }
}
