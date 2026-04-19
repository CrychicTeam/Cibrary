package org.pickaid.pibrary.api.registrate.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.Codec;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.api.registrate.PiRegistrate;
import org.pickaid.pibrary.runtime.registrate.registry.PiRegistryCatalog;

class PiDatapackRegistryBuilderTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        try {
            Bootstrap.bootStrap();
        } catch (ExceptionInInitializerError exception) {
            if (!isKnownNetworkBootstrapIssue(exception)) {
                throw exception;
            }
        }

        assertNotNull(CreativeModeTabs.SEARCH);
    }

    @AfterEach
    void clearCatalog() {
        try {
            Method method = PiRegistryCatalog.class.getDeclaredMethod("clearForTests");
            method.setAccessible(true);
            method.invoke(null);
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to clear PiRegistryCatalog for tests", exception);
        }
    }

    @Test
    void registerCapturesDirectAndNetworkCodec() {
        PiRegistrate registrate = createRegistrate();

        PiDatapackRegistryHandle<String> handle = registrate.datapackRegistry("spell_preset", Codec.STRING, Codec.STRING)
                .syncToClient()
                .register();

        assertEquals("pickaid:spell_preset", handle.registryKey().location().toString());
        assertSame(Codec.STRING, handle.codec());
        assertSame(Codec.STRING, handle.networkCodec());
        assertTrue(handle.syncToClient());
        assertSame(handle, PiRegistryCatalog.require(handle.registryKey()));
    }

    @Test
    void registerCanDisableClientSync() {
        PiRegistrate registrate = createRegistrate();

        PiDatapackRegistryHandle<String> handle = registrate.datapackRegistry("spell_preset", Codec.STRING, Codec.STRING)
                .noClientSync()
                .register();

        assertFalse(handle.syncToClient());
    }

    private static PiRegistrate createRegistrate() {
        RecordingEventBus bus = RecordingEventBus.create();
        try {
            Method method = PiRegistrate.class.getDeclaredMethod("create", String.class, IEventBus.class);
            method.setAccessible(true);
            return (PiRegistrate) method.invoke(null, "pickaid", bus.bus());
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to construct PiRegistrate through injected-bus seam", exception);
        }
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
