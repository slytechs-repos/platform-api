package com.slytechs.jnet.jnetruntime.pipeline;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides functionality to create a reusable proxy for instances of a
 * functional interface. This class uses method handles and dynamic proxies to
 * create a wrapper that forwards all method invocations to the current instance
 * while allowing for additional data handling.
 *
 * @author Mark Bednarczyk
 * @version 2.0
 * @since 2023-05-31
 */
public class DataProxy<T> {

	private final AtomicReference<T> instanceRef = new AtomicReference<>();
	private final Class<T> functionalInterfaceClass;
	private final Object data;
	private final T proxy;
	private final Method invokeMethod;

	/**
	 * Creates a new DataProxyMethodHandle for the specified functional interface.
	 *
	 * @param functionalInterfaceClass The Class object representing the functional
	 *                                 interface.
	 * @param data                     The data to be forwarded along with method
	 *                                 invocations.
	 * @throws IllegalArgumentException If no suitable method is found in the
	 *                                  functional interface.
	 */
	public DataProxy(Class<T> functionalInterfaceClass, Object data) {
		this.functionalInterfaceClass = functionalInterfaceClass;
		this.data = data;
		this.invokeMethod = getInvokeMethod(functionalInterfaceClass);
		this.proxy = createProxy();
		
		this.invokeMethod.setAccessible(true);
	}

	/**
	 * Sets a new instance for the proxy to delegate to.
	 *
	 * @param instance The new instance of the functional interface.
	 * @throws IllegalArgumentException If the instance is null.
	 */
	public void setInstance(T instance) {
		instanceRef.set(instance);
	}

	/**
	 * Gets the proxy instance that wraps the current functional interface instance.
	 *
	 * @return The proxy instance.
	 */
	public T getProxy() {
		return proxy;
	}

	@SuppressWarnings("unchecked")
	private T createProxy() {

		return (T) Proxy.newProxyInstance(
				functionalInterfaceClass.getClassLoader(),
				new Class<?>[] { functionalInterfaceClass },
				(proxy, method, args) ->
				{
                    method.setAccessible(true);
					
					T instance = instanceRef.get();
					if (instance == null) {
						throw new IllegalStateException("No instance set for proxy");
					}

					if (method.equals(invokeMethod)) {
						MethodHandle handle = MethodHandles.lookup()
								.unreflect(invokeMethod)
								.bindTo(instance);

						// Check if the method can accept an additional parameter
						if (args != null && args.length < method.getParameterCount()) {
							// If it can, add the data as the last argument
							Object[] newArgs = new Object[args.length + 1];
							System.arraycopy(args, 0, newArgs, 0, args.length);
							newArgs[args.length] = data;

							return handle.invokeWithArguments(newArgs);

						} else {
							// If it can't, just call the method with the original arguments
							return handle.invokeWithArguments(args);
						}
					}

					return method.invoke(instance, args);
				});
	}

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
	 * Main method demonstrating the usage of the DataProxyMethodHandle.
	 *
	 * @param args Command line arguments (not used).
	 */
	public static void main(String[] args) {
		// Create a proxy for DataOperation
		DataProxy<DataOperation> proxyHandle = new DataProxy<>(DataOperation.class,
				"Additional Data");

		// Set an initial instance
		DataOperation original1 = x -> {
			System.out.println("Processing " + x + " with instance 1");
			return x * 2;
		};
		proxyHandle.setInstance(original1);

		// Use the proxy
		DataOperation proxied = proxyHandle.getProxy();
		int result1 = proxied.operate(5);
		System.out.println("Result 1: " + result1);

		// Set a new instance
		DataOperation original2 = x -> {
			System.out.println("Processing " + x + " with instance 2");
			return x * 3;
		};
		proxyHandle.setInstance(original2);

		// Use the same proxy with the new instance
		int result2 = proxied.operate(5);
		System.out.println("Result 2: " + result2);

		// Example with Runnable
		DataProxy<Runnable> runnableProxyHandle = new DataProxy<>(Runnable.class,
				"Runnable Data");

		Runnable originalRunnable1 = () -> System.out.println("Original runnable 1");
		runnableProxyHandle.setInstance(originalRunnable1);

		Runnable proxiedRunnable = runnableProxyHandle.getProxy();
		proxiedRunnable.run();

		Runnable originalRunnable2 = () -> System.out.println("Original runnable 2");
		runnableProxyHandle.setInstance(originalRunnable2);

		proxiedRunnable.run();
	}

	/**
	 * A functional interface for demonstration purposes.
	 */
	@FunctionalInterface
	interface DataOperation {
		/**
		 * Performs this operation on the given argument.
		 *
		 * @param x The input argument.
		 * @return The result of the operation.
		 */
		int operate(int x);
	}
}