package com.slytechs.jnet.jnetruntime.internal.util.function;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;

/**
 * Utility class providing functionality to work with functional interfaces and
 * create dynamic proxies.
 * 
 * <p>
 * This class provides methods to:
 * <ul>
 * <li>Check if a class represents a functional interface
 * <li>Filter arrays of classes to identify functional interfaces
 * <li>Create dynamic proxies that forward calls to arrays of functional
 * interface implementations
 * <li>Create no-op proxy implementations of functional interfaces
 * </ul>
 */
public final class FunctionalProxies {

	/**
	 * Creates a no-op proxy for a generic functional interface type.
	 * 
	 * <p>
	 * Example usage:
	 * 
	 * <pre>{@code
	 * 
	 * // Create no-op Consumer<String>
	 * Consumer<String> noOp = FunctionalProxies.createNoOpProxy(new TypeLiteral<Consumer<String>>() {});
	 * noOp.accept("test"); // Does nothing
	 * 
	 * // Create no-op Consumer<Integer>
	 * Consumer<Integer> intNoOp = FunctionalProxies.createNoOpProxy(new TypeLiteral<Consumer<Integer>>() {});
	 * intNoOp.accept(42); // Does nothing
	 * }</pre>
	 *
	 * @param <T>   the functional interface type
	 * @param token the type token representing the generic functional interface
	 * @return a no-op proxy implementation of the functional interface
	 * @throws IllegalArgumentException if the type is not a functional interface
	 */
	public static <T> T createNoOpProxy(TypeLiteral<T> token) {
		Class<T> rawType = token.getGenericClass();
		return createNoOpProxy(rawType);
	}

	/**
	 * Private constructor to prevent instantiation of utility class.
	 */
	private FunctionalProxies() {
		throw new AssertionError("No FunctionalProxies instances for you!");
	}

	/**
	 * Checks if the given class represents a functional interface.
	 * 
	 * <p>
	 * A functional interface is defined as an interface with exactly one abstract
	 * method. Default methods and static methods are not counted as abstract
	 * methods.
	 * 
	 * @param <T>  the type of the class being checked
	 * @param type the class to check
	 * @return {@code true} if the class represents a functional interface,
	 *         {@code false} otherwise
	 * 
	 * @see java.lang.FunctionalInterface
	 */
	private static <T> boolean isFunctionalInterface(Class<T> type) {
		if (!type.isInterface()) {
			return false;
		}

		return Arrays.stream(type.getMethods())
				.filter(m -> Modifier.isAbstract(m.getModifiers()))
				.count() == 1;
	}

	/**
	 * Filters an array of classes to return only those that are functional
	 * interfaces.
	 * 
	 * <p>
	 * This method examines each class in the input array and includes it in the
	 * result only if it represents a functional interface. The order of classes in
	 * the result array matches their order in the input array.
	 * 
	 * <p>
	 * Example usage:
	 * 
	 * <pre>{@code
	 * Class<?>[] classes = {
	 * 		Runnable.class, // included
	 * 		String.class, // excluded (not an interface)
	 * 		IntConsumer.class // included
	 * };
	 * Class<?>[] functional = FunctionalProxies.filterFunctionalInterfaces(classes);
	 * }</pre>
	 * 
	 * @param <T>     the type of the functional interfaces
	 * @param classes the array of classes to filter
	 * @return an array containing only the classes that represent functional
	 *         interfaces
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T>[] filterFunctionalInterfaces(Class<?>... classes) {
		return Arrays.stream(classes)
				.filter(FunctionalProxies::isFunctionalInterface)
				.toArray(Class[]::new);
	}

	/**
	 * Creates a proxy implementation of a functional interface that does nothing
	 * when invoked.
	 * 
	 * <p>
	 * The returned proxy will implement the given functional interface but its
	 * method invocations will be ignored. This is useful for testing or as a
	 * placeholder implementation.
	 * 
	 * <p>
	 * Example usage:
	 * 
	 * <pre>{@code
	 * // Create no-op consumer
	 * IntConsumer noOp = FunctionalProxies.createNoOpProxy(IntConsumer.class);
	 * noOp.accept(42); // Does nothing
	 * 
	 * // Create no-op runnable
	 * Runnable noOpRun = FunctionalProxies.createNoOpProxy(Runnable.class);
	 * noOpRun.run(); // Does nothing
	 * }</pre>
	 * 
	 * @param <T>                 the type of the functional interface
	 * @param functionalInterface the class object representing the functional
	 *                            interface
	 * @return a proxy instance that implements the interface but does nothing when
	 *         invoked
	 * @throws IllegalArgumentException if the class is not a functional interface
	 */
	@SuppressWarnings("unchecked")
	public static <T> T createNoOpProxy(Class<T> functionalInterface) {
		if (!isFunctionalInterface(functionalInterface)) {
			throw new IllegalArgumentException("Not a functional interface: " + functionalInterface);
		}

		return (T) Proxy.newProxyInstance(
				functionalInterface.getClassLoader(),
				new Class<?>[] { functionalInterface
				},
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						// Return default value based on return type
						Class<?> returnType = method.getReturnType();
						if (returnType == void.class) {
							return null;
						} else if (returnType.isPrimitive()) {
							if (returnType == boolean.class)
								return false;
							if (returnType == char.class)
								return '\0';
							return 0;
						}
						return null;
					}
				});
	}

	@SuppressWarnings("unchecked")
	public static <T> IntFunction<T[]> createArrayAllocator(Class<T> dataType) {
		return size -> (T[]) Array.newInstance(dataType, size);
	}

	/**
	 * Creates a proxy that forwards calls to all elements in an array of functional
	 * interface implementations. Only works for functional interfaces with void
	 * return type.
	 * 
	 * <p>
	 * The returned function takes an array of functional interface implementations
	 * and returns a proxy that forwards method calls to each element in the array.
	 * 
	 * <p>
	 * Example usage:
	 * 
	 * <pre>{@code
	 * Function<IntConsumer[], IntConsumer> wrapper = FunctionalProxies.createArrayWrapper(IntConsumer.class);
	 * 
	 * IntConsumer[] array = {
	 * 		i -> System.out.println("First: " + i),
	 * 		i -> System.out.println("Second: " + i)
	 * };
	 * 
	 * IntConsumer proxy = wrapper.apply(array);
	 * proxy.accept(42); // Calls both consumers
	 * }</pre>
	 * 
	 * @param <T>                 the type of the functional interface
	 * @param functionalInterface the class object representing the functional
	 *                            interface
	 * @return a function that creates a proxy forwarding calls to all array
	 *         elements
	 * @throws IllegalArgumentException if the class is not a functional interface
	 *                                  or if the functional method does not return
	 *                                  void
	 */
	@SuppressWarnings("unchecked")
	public static <T> Function<T[], T> createArrayWrapper(Class<T> functionalInterface) {
		if (!isFunctionalInterface(functionalInterface)) {
			throw new IllegalArgumentException("Not a functional interface: " + functionalInterface);
		}

		Method functionalMethod = Arrays.stream(functionalInterface.getMethods())
				.filter(m -> Modifier.isAbstract(m.getModifiers()))
				.findFirst()
				.orElseThrow();

		if (functionalMethod.getReturnType() != void.class) {
			throw new IllegalArgumentException("Method must return void: " + functionalMethod);
		}

		return array -> (T) Proxy.newProxyInstance(
				functionalInterface.getClassLoader(),
				new Class<?>[] { functionalInterface
				},
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						for (T element : array) {
							try {
								method.invoke(element, args);
							} catch (Exception e) {
								throw new RuntimeException("Error invoking " + method, e);
							}
						}
						return null;
					}
				});
	}

	// Example usage:
	public static void main(String[] args) {
		// Create wrapper function for IntConsumer
		Function<IntConsumer[], IntConsumer> intConsumerWrapper = createArrayWrapper(IntConsumer.class);

		// Create test array
		IntConsumer[] consumers = {
				i -> System.out.println("First consumer: " + i),
				i -> System.out.println("Second consumer: " + i)
		};

		// Create wrapper
		IntConsumer wrapper = intConsumerWrapper.apply(consumers);

		// Test it
		wrapper.accept(42);

		// Compare with manual implementation
		IntConsumer manualWrapper = i -> {
			for (var e : consumers)
				e.accept(i);
		};

		manualWrapper.accept(42);
	}
}