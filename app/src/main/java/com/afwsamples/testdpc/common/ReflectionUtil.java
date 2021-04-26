package com.afwsamples.testdpc.common;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Common utility functions for reflection. These are intended to be used to test APIs before they
 * are added to the SDK. There should be not uses of this class checked in to the repository.
 */
public final class ReflectionUtil {

    private static final String TAG = ReflectionUtil.class.getSimpleName();

    /**
     * Calls a method on an object with the given arguments. This can be used when the method is not
     * in the SDK. If any arguments are {@code null} or primitive types you must use
     * {@link #invoke(Object, String, Class[], Object...)} to avoid {@link NullPointerException}s
     * and autoboxing respectively.
     *
     * ReflectionUtil.invoke(dpm, "clearDeviceOwnerApp", "com.example.deviceowner");
     *
     * @param obj        The object to call the method on.
     * @param methodName The name of the method.
     * @param args       The arguments to pass to the method. Ensure they are of the same type as
     *                   the method parameters. If they are not, use the more verbose
     *                   {@link #invoke(Object, String, Class[], Object[])}.
     * @return           The result of the invocation. {@code null} if {@code void}.
     */
    public static <T> T invoke(Object obj, String methodName, Object... args)
            throws ReflectionIsTemporaryException {
        return invoke(obj.getClass(), obj, methodName, args);
    }

    /**
     * Same as {@link #invoke(Object, String, Object...)} but for static methods.
     */
    public static <T> T invoke(Class<?> clazz, String methodName, Object... args)
            throws ReflectionIsTemporaryException {
        return invoke(clazz, null, methodName, args);
    }

    /**
     * Calls a method on an object with the given arguments. This can be used when the method is not
     * in the SDK. Consider using {@link #invoke(Object, String, Object...)} for less verbosity if
     * the arguments are the same type as the method parameters (subclasses don't work).
     *
     * ReflectionUtil.invoke(dpm, "wipeData", new Class<?>[] {int.class},
     *         ReflectionUtil.intConstant(DevicePolicyManager.class, "WIPE_EXTERNAL_STORAGE"));
     *
     * @param obj            The object to call the method on.
     * @param methodName     The name of the method.
     * @param parameterTypes The method parameter types.
     * @param args           The arguments to pass to the method.
     * @return               The result of the invocation. {@code null} if {@code void}.
     */
    public static <T> T invoke(Object obj, String methodName, Class<?>[] parameterTypes,
                                Object... args) throws ReflectionIsTemporaryException {
        return invoke(obj.getClass(), obj, methodName, parameterTypes, args);
    }

    /**
     * Same as {@link #invoke(Object, String, Class[], Object...)} but for static methods.
     */
    public static <T> T invoke(Class<?> clazz, String methodName, Class<?>[] parameterTypes,
                                Object... args) throws ReflectionIsTemporaryException {
        return invoke(clazz, null, methodName, parameterTypes, args);
    }

    /** Resolve the parameter types and invoke the method. */
    private static <T> T invoke(Class<?> clazz, Object obj, String methodName, Object... args)
            throws ReflectionIsTemporaryException {
        Class<?> parameterTypes[] = new Class<?>[args.length];
        for (int i = 0; i < args.length; ++i) {
            parameterTypes[i] = args[i].getClass();
        }
        return invoke(clazz, obj, methodName, parameterTypes, args);
    }

    /** Resolve the method and invoke it. */
    private static <T> T invoke(Class<?> clazz, Object obj, String methodName,
                                 Class<?>[] parameterTypes, Object... args)
            throws ReflectionIsTemporaryException {
        try {
            @SuppressWarnings("unchecked")
            T result = (T) clazz.getMethod(methodName, parameterTypes).invoke(obj, args);
            return result;
        } catch (SecurityException | NoSuchMethodException | IllegalArgumentException
                | IllegalAccessException | InvocationTargetException e) {
            ReflectionIsTemporaryException.rethrow(e, clazz, methodName, args);
            return null;
        }
    }

    /**
     * Gets the value of a static int constant. This can be used when the constant is not yet in the
     * SDK.
     *
     * ReflectionUtil.intConstant(DevicePolicyManager.class, "WIPE_EXTERNAL_STORAGE");
     *
     * @param clazz     The class that contains the constant.
     * @param fieldName The name of the constant field.
     * @return          The value of the constant.
     */
    public static int intConstant(Class<?> clazz, String fieldName)
            throws ReflectionIsTemporaryException {
        try {
            return clazz.getField(fieldName).getInt(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ReflectionIsTemporaryException("Failed to retrieve constant", e);
        }
    }

    /**
     * Gets the value of a static string constant. This can be used when the constant is not yet in
     * the SDK.
     *
     * ReflectionUtil.stringConstant(DevicePolicyManager.class, "ACTION_ADD_DEVICE_ADMIN");
     *
     * @param clazz     The class that contains the constant.
     * @param fieldName The name of the constant field.
     * @return          The value of the constant.
     */
    public static String stringConstant(Class<?> clazz, String fieldName)
            throws ReflectionIsTemporaryException {
        try {
            return (String) clazz.getField(fieldName).get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ReflectionIsTemporaryException("Failed to retrieve constant", e);
        }
    }

    /**
     * Thrown when the temporary use of reflection fails to find and use a new API.
     *
     * This will happen when the API is not yet available in the SDK and TestDPC is running on
     * device that isn't running the latest version of the framework and therefore doesn't support
     * the API.
     *
     * To handle this, gracefully fail the operation in progress.
     */
    public static final class ReflectionIsTemporaryException extends Exception {

        private static final long serialVersionUID = 1L;

        private ReflectionIsTemporaryException(String message, Throwable cause) {
            super(message, cause);
        }

        public static void rethrow(Exception e, Class<?> clazz, String methodName, Object... args)
                throws ReflectionIsTemporaryException {
            String method = clazz.getSimpleName() + "." + methodName + "("
                + (args == null || args.length == 0 ? "" : Arrays.toString(args)) + ")";
            Log.w(TAG, "Exception calling method " + method + ":", e);
            if (e instanceof InvocationTargetException) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }
            }
            throw new ReflectionIsTemporaryException("Failed to invoke " + method, e);
        }
    }

    private ReflectionUtil() {
        throw new UnsupportedOperationException("provides only static methods");
    }
}
