package org.pickaid.pibrary.api.registrate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.Codec;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegisterEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.pickaid.pibrary.dev.example.CounterBlockEntity;
import org.pickaid.pibrary.dev.example.CounterChunkService;
import org.pickaid.pibrary.dev.example.CounterLevelService;
import org.pickaid.pibrary.dev.example.CounterPlayerService;
import org.pickaid.pibrary.dev.example.CounterState;

class PiRegistrateTest {
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

    @Test
    void createBuildsStableContextAndIds() {
        RecordingEventBus bus = RecordingEventBus.create();
        PiRegistrate registrate = PiRegistrate.create("pickaid", bus.bus());

        assertEquals("pickaid", registrate.getModid());
        assertEquals("pickaid:test_path", registrate.id("test_path").toString());
        assertSame(registrate.context(), registrate.context());
        assertEquals("pickaid", registrate.context().modId());
        assertSame(bus.bus(), registrate.getModEventBus());
        assertTrue(bus.listenerTypes().contains(RegisterEvent.class));
        assertTrue(bus.listenerTypes().contains(BuildCreativeModeTabContentsEvent.class));
        assertTrue(bus.listenerTypes().contains(FMLCommonSetupEvent.class));
        assertTrue(bus.listenerCount() >= 4);
    }

    @Test
    void defaultsRejectBlankPath() {
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> PiRegistrateDefaults.requirePath("  "));

        assertEquals("path must not be blank", exception.getMessage());
    }

    @Test
    void rootExposesTypedBuilderEntryPoints() {
        RecordingEventBus bus = RecordingEventBus.create();
        PiRegistrate registrate = PiRegistrate.create("pickaid", bus.bus());

        assertNotNull(registrate.customRegistry("spell_type", String.class));
        assertNotNull(registrate.datapackRegistry("spell_preset", Codec.STRING, Codec.STRING));
        assertNotNull(registrate.entityService("counter_entity", CounterState.class, CounterPlayerService::new));
        assertNotNull(registrate.playerService("counter_player", CounterState.class, CounterPlayerService::new));
        assertNotNull(registrate.chunkService("counter_chunk", CounterState.class, CounterChunkService::new));
        assertNotNull(registrate.levelService("counter_level", CounterState.class, CounterLevelService::new));
        assertNotNull(registrate.blockEntityService("counter_block", CounterState.class, CounterBlockEntity.class));
        assertNotNull(registrate.config("combat", Codec.INT, 5));
        assertNotNull(registrate.creativeTab("main", "PickAID"));
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
        private final List<Class<?>> listenerTypes = new ArrayList<>();
        private int addListenerCalls;
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

        List<Class<?>> listenerTypes() {
            return listenerTypes;
        }

        int listenerCount() {
            return addListenerCalls;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "register", "unregister", "shutdown", "start" -> null;
                case "post" -> Boolean.FALSE;
                case "addListener" -> {
                    addListenerCalls++;
                    recordListenerType(args);
                    yield null;
                }
                case "addGenericListener" -> null;
                case "toString" -> "RecordingEventBus";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> defaultValue(method.getReturnType());
            };
        }

        private void recordListenerType(Object[] args) {
            if (args == null || args.length < 3 || !(args[2] instanceof Class<?> listenerType)) {
                return;
            }
            listenerTypes.add(listenerType);
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
