package org.pickaid.pibrary.api.registrate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;

public final class PiRegistrateTestSupport {
    private static volatile boolean minecraftBootstrapped;

    private PiRegistrateTestSupport() {
    }

    public static PiRegistrate create(String modId) {
        ensureMinecraftBootstrapped();
        RecordingEventBus bus = RecordingEventBus.create();
        try {
            Method method = PiRegistrate.class.getDeclaredMethod("create", String.class, IEventBus.class);
            method.setAccessible(true);
            return (PiRegistrate) method.invoke(null, modId, bus.bus());
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to construct PiRegistrate through injected-bus seam", exception);
        }
    }

    private static synchronized void ensureMinecraftBootstrapped() {
        if (minecraftBootstrapped) {
            return;
        }

        SharedConstants.tryDetectVersion();
        try {
            Bootstrap.bootStrap();
        } catch (ExceptionInInitializerError exception) {
            if (!isKnownNetworkBootstrapIssue(exception)) {
                throw exception;
            }
        }

        if (CreativeModeTabs.SEARCH == null) {
            throw new AssertionError("Minecraft bootstrap did not initialize creative tabs");
        }
        minecraftBootstrapped = true;
    }

    private static boolean isKnownNetworkBootstrapIssue(ExceptionInInitializerError exception) {
        Throwable cause = exception.getCause();
        if (!(cause instanceof RuntimeException runtimeException)) {
            return false;
        }

        return runtimeException.getMessage() != null
                && runtimeException.getMessage().contains("Error computing listener list for net.minecraftforge.network.NetworkEvent");
    }

    private static final class RecordingEventBus implements InvocationHandler {
        private final IEventBus bus;

        private RecordingEventBus() {
            this.bus = (IEventBus)
                    Proxy.newProxyInstance(IEventBus.class.getClassLoader(), new Class<?>[] {IEventBus.class}, this);
        }

        static RecordingEventBus create() {
            return new RecordingEventBus();
        }

        IEventBus bus() {
            return bus;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "register", "unregister", "shutdown", "start", "addListener", "addGenericListener" -> null;
                case "post" -> Boolean.FALSE;
                case "toString" -> "RecordingEventBus";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object defaultValue(Class<?> returnType) {
            if (returnType == void.class) {
                return null;
            }
            if (returnType == boolean.class) {
                return false;
            }
            if (returnType == int.class) {
                return 0;
            }
            if (returnType == long.class) {
                return 0L;
            }
            if (returnType == double.class) {
                return 0.0d;
            }
            if (returnType == float.class) {
                return 0.0f;
            }
            if (returnType == short.class) {
                return (short) 0;
            }
            if (returnType == byte.class) {
                return (byte) 0;
            }
            if (returnType == char.class) {
                return '\0';
            }
            return null;
        }
    }
}
