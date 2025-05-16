package org.pickaid.pibrary.tools.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;

public final class ReflectionUtils {
    private static final ConcurrentHashMap<String, MethodHandle> METHOD_HANDLE_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, VarHandle> VAR_HANDLE_CACHE = new ConcurrentHashMap<>();

    public static MethodHandles.Lookup lookup() {
        return MethodHandles.lookup();
    }

    public static MethodHandles.Lookup privateLookupIn(Class<?> targetClass, MethodHandles.Lookup lookup) {
        try {
            return MethodHandles.privateLookupIn(targetClass, lookup);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot elevate access privileges", e);
        }
    }

    public static MethodHandle findVirtual(MethodHandles.Lookup lookup, Class<?> targetClass,
                                           String methodName, Class<?> returnType, Class<?>... paramTypes) {
        String cacheKey = targetClass.getName() + "#" + methodName + "#virtual";
        return METHOD_HANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                MethodType methodType = MethodType.methodType(returnType, paramTypes);
                return lookup.findVirtual(targetClass, methodName, methodType);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException("Failed to find instance method: " + methodName, e);
            }
        });
    }

    public static MethodHandle findStatic(MethodHandles.Lookup lookup, Class<?> targetClass,
                                          String methodName, Class<?> returnType, Class<?>... paramTypes) {
        String cacheKey = targetClass.getName() + "#" + methodName + "#static";
        return METHOD_HANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                MethodType methodType = MethodType.methodType(returnType, paramTypes);
                return lookup.findStatic(targetClass, methodName, methodType);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException("Failed to find static method: " + methodName, e);
            }
        });
    }

    public static MethodHandle findConstructor(MethodHandles.Lookup lookup, Class<?> targetClass,
                                               Class<?>... paramTypes) {
        String cacheKey = targetClass.getName() + "#constructor" + paramTypes.length;
        return METHOD_HANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                MethodType methodType = MethodType.methodType(void.class, paramTypes);
                return lookup.findConstructor(targetClass, methodType);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException("Failed to find constructor", e);
            }
        });
    }

    public static MethodHandle findSpecial(MethodHandles.Lookup lookup, Class<?> targetClass,
                                           String methodName, Class<?> declaringClass,
                                           Class<?> returnType, Class<?>... paramTypes) {
        String cacheKey = targetClass.getName() + "#" + methodName + "#special";
        return METHOD_HANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                MethodType methodType = MethodType.methodType(returnType, paramTypes);
                return lookup.findSpecial(declaringClass, methodName, methodType, targetClass);
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException("Failed to find special method: " + methodName, e);
            }
        });
    }

    public static VarHandle findVarHandle(MethodHandles.Lookup lookup, Class<?> targetClass,
                                          String fieldName, Class<?> fieldType) {
        String cacheKey = targetClass.getName() + "#" + fieldName + "#instance";
        return VAR_HANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                return lookup.findVarHandle(targetClass, fieldName, fieldType);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to find instance field: " + fieldName, e);
            }
        });
    }

    public static VarHandle findStaticVarHandle(MethodHandles.Lookup lookup, Class<?> targetClass,
                                                String fieldName, Class<?> fieldType) {
        String cacheKey = targetClass.getName() + "#" + fieldName + "#static";
        return VAR_HANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
            try {
                return lookup.findStaticVarHandle(targetClass, fieldName, fieldType);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to find static field: " + fieldName, e);
            }
        });
    }

    public static VarHandle arrayElementVarHandle(Class<?> arrayClass) {
        if (!arrayClass.isArray()) {
            throw new IllegalArgumentException("Class must be an array type");
        }
        String cacheKey = arrayClass.getName() + "#arrayElement";
        return VAR_HANDLE_CACHE.computeIfAbsent(cacheKey, k -> {
            return MethodHandles.arrayElementVarHandle(arrayClass);
        });
    }

    public static MethodHandle adaptMethodType(MethodHandle handle, MethodType newType) {
        return handle.asType(newType);
    }

    public static MethodHandle bindTo(MethodHandle handle, Object receiver) {
        return handle.bindTo(receiver);
    }

    public static MethodHandles.Lookup getImplLookup() {
        int javaVersion = getJavaVersion();

        if (javaVersion <= 15) {
            return getImplLookupLegacy();
        } else if (javaVersion < 24) {
            return getImplLookupMiddle();
        } else {
            return getImplLookupModern();
        }
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            return Integer.parseInt(version.substring(2, 3));
        }
        int dot = version.indexOf('.');
        if (dot != -1) {
            return Integer.parseInt(version.substring(0, dot));
        }
        return Integer.parseInt(version);
    }

    private static MethodHandles.Lookup getImplLookupLegacy() {
        try {
            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            implLookupField.setAccessible(true);
            return (MethodHandles.Lookup) implLookupField.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get IMPL_LOOKUP", e);
        }
    }

    private static MethodHandles.Lookup getImplLookupMiddle() {
        try {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                    .getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);

            int allModes = Modifier.PRIVATE | Modifier.PROTECTED | Modifier.PUBLIC;
            return constructor.newInstance(Object.class, allModes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get IMPL_LOOKUP for Java 16-23", e);
        }
    }

    private static MethodHandles.Lookup getImplLookupModern() {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Object unsafe = theUnsafeField.get(null);

            Method getObjectMethod = unsafeClass.getMethod("getObject", Object.class, long.class);
            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");

            Method objectFieldOffsetMethod = unsafeClass.getMethod("objectFieldOffset", Field.class);
            long offset = (long) objectFieldOffsetMethod.invoke(unsafe, implLookupField);

            return (MethodHandles.Lookup) getObjectMethod.invoke(unsafe, MethodHandles.Lookup.class, offset);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get IMPL_LOOKUP for Java 24+", e);
        }
    }

    public static Object safeInvoke(MethodHandle handle, Object... args) {
        try {
            return handle.invokeWithArguments(args);
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new RuntimeException("Error invoking method handle", t);
            }
        }
    }

    public static void clearCaches() {
        METHOD_HANDLE_CACHE.clear();
        VAR_HANDLE_CACHE.clear();
    }

    private ReflectionUtils() {}
}