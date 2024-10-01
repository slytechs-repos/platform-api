/*
 * Sly Technologies Free License
 * 
 * Copyright 2024 Sly Technologies Inc.
 *
 * Licensed under the Sly Technologies Free License (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.slytechs.com/free-license-text
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.slytechs.jnet.jnetruntime.pipeline;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

/**
 * Provides functionality to wrap an array of functional interfaces into a
 * single instance. This class uses method handles and dynamic proxies to create
 * a combined instance that invokes the method on all elements of the array when
 * called.
 *
 * @author Mark Bednarczyk
 */
public class DataArrayMethodHandle {

	/**
	 * A functional interface for demonstration purposes.
	 *
	 * @author Mark Bednarczyk
	 */
	@FunctionalInterface
	interface IntOperation {
		/**
		 * Performs this operation on the given argument.
		 *
		 * @param x The input argument.
		 * @return The result of the operation.
		 */
		int operate(int x);
	}

	/**
	 * Creates a combined MethodHandle that invokes all the given handles.
	 *
	 * @param handles    An array of MethodHandles to be combined.
	 * @param returnType The return type of the method.
	 * @return A combined MethodHandle that invokes all the given handles.
	 */
	private static MethodHandle createCombinedMethodHandle(MethodHandle[] handles, Class<?> returnType) {
		if (returnType == void.class) {
			return createVoidCombinedMethodHandle(handles);
		} else {
			return createNonVoidCombinedMethodHandle(handles);
		}
	}

	/**
	 * Creates a combined MethodHandle for non-void return type methods.
	 *
	 * @param handles An array of MethodHandles to be combined.
	 * @return A combined MethodHandle that invokes all the given handles, returning
	 *         the result of the last handle.
	 */
	private static MethodHandle createNonVoidCombinedMethodHandle(MethodHandle[] handles) {
		MethodHandle combinedHandle = handles[handles.length - 1];
		for (int i = handles.length - 2; i >= 0; i--) {
			MethodHandle nextHandle = handles[i];
			combinedHandle = MethodHandles.foldArguments(
					combinedHandle,
					MethodHandles.dropReturn(nextHandle));
		}
		return combinedHandle;
	}

	/**
	 * Creates a no-op instance of the given functional interface.
	 *
	 * @param <T>           The type of the functional interface.
	 * @param interfaceType The Class object representing the functional interface.
	 * @param method        The Method object representing the single abstract
	 *                      method of the functional interface.
	 * @return A no-op instance of the functional interface.
	 */
	@SuppressWarnings("unchecked")
	private static <T> T createNoOpInstance(Class<T> interfaceType, Method method) {
		MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());

		MethodHandle noOpHandle;
		if (method.getReturnType() == void.class) {
			noOpHandle = MethodHandles.empty(methodType);
		} else {
			Object defaultValue = getDefaultValue(method.getReturnType());
			MethodHandle constHandle = MethodHandles.constant(method.getReturnType(), defaultValue);
			noOpHandle = MethodHandles.dropArguments(constHandle, 0, method.getParameterTypes());
		}

		return (T) Proxy.newProxyInstance(
				interfaceType.getClassLoader(),
				new Class<?>[] { interfaceType },
				(proxy, m, args) ->
				{
					if (m.equals(method)) {
						return noOpHandle.invokeWithArguments(args);
					}
					throw new UnsupportedOperationException("Unexpected method call on no-op instance");
				});
	}

	/**
	 * Creates a combined MethodHandle for void return type methods.
	 *
	 * @param handles An array of MethodHandles to be combined.
	 * @return A combined MethodHandle that invokes all the given handles, ignoring
	 *         return values.
	 */
	private static MethodHandle createVoidCombinedMethodHandle(MethodHandle[] handles) {
		MethodHandle combinedHandle = handles[0];
		for (int i = 1; i < handles.length; i++) {
			MethodHandle nextHandle = handles[i];
			combinedHandle = MethodHandles.foldArguments(
					MethodHandles.dropReturn(nextHandle),
					combinedHandle);
		}
		return combinedHandle;
	}

	/**
	 * Returns the default value for the given type.
	 *
	 * @param type The Class object representing the type.
	 * @return The default value for the given type (0 for numeric types, false for
	 *         boolean, '\u0000' for char, null for objects).
	 */
	private static Object getDefaultValue(Class<?> type) {
		if (type.equals(boolean.class))
			return false;
		if (type.equals(char.class))
			return '\u0000';
		if (type.equals(byte.class))
			return (byte) 0;
		if (type.equals(short.class))
			return (short) 0;
		if (type.equals(int.class))
			return 0;
		if (type.equals(long.class))
			return 0L;
		if (type.equals(float.class))
			return 0.0f;
		if (type.equals(double.class))
			return 0.0d;
		return null;
	}

	/**
	 * Finds the single abstract method in the given functional interface.
	 *
	 * @param functionalInterfaceClass The Class object representing the functional
	 *                                 interface.
	 * @return The Method object representing the single abstract method of the
	 *         functional interface.
	 * @throws IllegalArgumentException If multiple non-default methods are found or
	 *                                  if no suitable method is found.
	 */
	private static Method getInvokeMethod(Class<?> functionalInterfaceClass) {
		Method[] methods = functionalInterfaceClass.getMethods();
		Method invokeMethod = null;
		int abstractMethodCount = 0;

		for (Method method : methods) {
			if (method.isDefault() || method.isBridge() ||
					Modifier.isStatic(method.getModifiers()) ||
					method.getDeclaringClass() == Object.class) {
				continue;
			}
			abstractMethodCount++;
			if (abstractMethodCount > 1) {
				throw new IllegalArgumentException("Multiple non-default methods found in interface " +
						functionalInterfaceClass.getName());
			}
			invokeMethod = method;
		}

		if (invokeMethod == null) {
			throw new IllegalArgumentException("No suitable method found in the functional interface " +
					functionalInterfaceClass.getName());
		}

		return invokeMethod;
	}

	/**
	 * Main method demonstrating the usage of the DataArrayMethodHandleWrapper.
	 *
	 * @param args Command line arguments (not used).
	 * @throws Throwable If an error occurs during the demonstration.
	 */
	public static void main(String[] args) throws Throwable {
		// Example with non-empty array of Runnable
		Runnable[] runnables = {
				() -> System.out.println("First runnable"),
				() -> System.out.println("Second runnable"),
				() -> System.out.println("Third runnable")
		};

		Runnable wrappedRunnable = wrapArray(runnables, Runnable.class);
		wrappedRunnable.run(); // This will run all three runnables

		// Example with empty array of Runnable
		Runnable[] emptyRunnables = new Runnable[0];
		Runnable noOpRunnable = wrapArray(emptyRunnables, Runnable.class);
		noOpRunnable.run(); // This will do nothing

		// Example with a functional interface that returns a value
		IntOperation[] operations = {
				x -> x + 1,
				x -> x * 2,
				x -> x - 3
		};

		IntOperation wrappedOperation = wrapArray(operations, IntOperation.class);
		int result = wrappedOperation.operate(5);
		System.out.println("Result: " + result); // Should print: Result: 7

		// Example with empty array of IntOperation
		IntOperation[] emptyOperations = new IntOperation[0];
		IntOperation noOpOperation = wrapArray(emptyOperations, IntOperation.class);
		int noOpResult = noOpOperation.operate(5);
		System.out.println("No-op Result: " + noOpResult); // Should print: No-op Result: 0
	}

	/**
	 * Wraps an array of functional interfaces into a single instance.
	 *
	 * @param <T>                      The type of the functional interface.
	 * @param array                    The array of functional interface instances
	 *                                 to be wrapped.
	 * @param functionalInterfaceClass The Class object representing the functional
	 *                                 interface.
	 * @return A single instance of the functional interface that invokes the method
	 *         on all array elements.
	 * @throws IllegalAccessException   the illegal access exception
	 * @throws IllegalArgumentException If the array is null or if no suitable
	 *                                  method is found in the functional interface.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T wrapArray(T[] array, Class<T> functionalInterfaceClass) throws IllegalAccessException {
		if (array == null) {
			throw new IllegalArgumentException("Array must not be null");
		}

		Method invokeMethod = getInvokeMethod(functionalInterfaceClass);

		if (array.length == 0) {
			return createNoOpInstance(functionalInterfaceClass, invokeMethod);
		}

		MethodHandle[] handles = new MethodHandle[array.length];
		for (int i = 0; i < array.length; i++) {
			handles[i] = MethodHandles.lookup().unreflect(invokeMethod).bindTo(array[i]);
		}

		MethodHandle combinedHandle = createCombinedMethodHandle(handles, invokeMethod.getReturnType());

		return (T) Proxy.newProxyInstance(
				functionalInterfaceClass.getClassLoader(),
				new Class<?>[] { functionalInterfaceClass },
				(proxy, method, args) ->
				{
					if (method.equals(invokeMethod)) {
						return combinedHandle.invokeWithArguments(args);
					}
					return method.invoke(array[0], args);
				});
	}
}