package com.slytechs.jnet.platform.api.domain.value.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.slytechs.jnet.platform.api.domain.Domain;
import com.slytechs.jnet.platform.api.domain.DomainBase;
import com.slytechs.jnet.platform.api.domain.DomainPath;
import com.slytechs.jnet.platform.api.domain.impl.RootDomain;
import com.slytechs.jnet.platform.api.domain.value.Value;
import com.slytechs.jnet.platform.api.domain.value.ValueFolder;
import com.slytechs.jnet.platform.api.domain.value.ValueFolderBase;
import com.slytechs.jnet.platform.api.domain.value.Values;

/**
 * Demonstration of Value framework capabilities and usage patterns with domain
 * integration and proper locking.
 */
public class ValueDemo {

	// Example domain class with values and folders
	static class ProtocolDomain extends DomainBase {
		private final Value version;
		private final Value mtu;
		private final ValueFolder headers;
		private final ValueFolder options;

		public ProtocolDomain(String name, Domain parent) {
			super(name, parent);

			// Create basic values within this domain
			this.version = Values.of("version", this, "1.0");
			this.mtu = Values.of("mtu", this, 1500);

			// Create value folders
			this.headers = new ValueFolderBase("headers", this, null);
			this.options = new ValueFolderBase("options", this, null);

			// Add header values
			headers.addValue(Values.of("type", this, 0x0800));
			headers.addValue(Values.of("length", this, 20));

			// Add option values with automatic registration cleanup
			options.addValue(Values.of("window-scale", this, 7), reg -> {
				System.out.println("Registered window-scale cleanup");
			});

			options.addValue(Values.of("timestamp", this, true), reg -> {
				System.out.println("Registered timestamp cleanup");
			});

			// Add folders to domain
			addFolder(headers);
			addFolder(options);
		}

		public Value getMtu() {
			return mtu;
		}

		public Value getVersion() {
			return version;
		}

		public ValueFolder getHeaders() {
			return headers;
		}

		public ValueFolder getOptions() {
			return options;
		}
	}

	public static void main(String[] args) {
		System.out.println("Value Framework Demonstration\n");

		// Create domain hierarchy
		Domain root = new RootDomain();
		ProtocolDomain ipv4 = new ProtocolDomain("ipv4", root);
		root.addDomain(ipv4);

		// 1. Basic value operations
		System.out.println("1. Basic Value Operations:");
		demonstrateBasicOperations(ipv4);

		// 2. Value transformation and composition
		System.out.println("\n2. Value Transformation and Composition:");
		demonstrateTransformation(ipv4);

		// 3. Domain and folder hierarchy
		System.out.println("\n3. Domain and Folder Hierarchy:");
		demonstrateHierarchy(root, ipv4);

		// 4. Thread safety
		System.out.println("\n4. Thread Safety:");
		demonstrateThreadSafety(ipv4);

		// 5. Advanced value types
		System.out.println("\n5. Advanced Value Types:");
		demonstrateAdvancedTypes(ipv4);
	}

	private static void demonstrateBasicOperations(ProtocolDomain domain) {
		// Create and modify values
		Value counter = Values.of("counter", domain, 0);
		System.out.println("Initial counter: " + counter.get());

		counter.set(42);
		System.out.println("After set: " + counter.get());

		boolean success = counter.compareAndSet(42, 100);
		System.out.println("CAS success: " + success + ", new value: " + counter.get());

		Object oldValue = counter.getAndSet(200);
		System.out.println("Old value: " + oldValue + ", new value: " + counter.get());

		// Demonstrate constant value
		Value constant = Values.constant("pi", domain, Math.PI);
		System.out.println("Constant value: " + constant.get());
		try {
			constant.set(3.14);
		} catch (UnsupportedOperationException e) {
			System.out.println("Cannot modify constant value (expected)");
		}
	}

	private static void demonstrateTransformation(ProtocolDomain domain) {
		// Create a value with transformation (hex string <-> integer)
		Value hexValue = Values.transform(
				Values.of("hex", domain, 255),
				Object::toString,
				str -> Integer.parseInt(str.toString(), 16));

		System.out.println("Hex value: " + hexValue.get());
		hexValue.set("FF");
		System.out.println("After set: " + hexValue.get());

		// Demonstrate computed value
		Value computed = Values.computed("timestamp", domain, System::currentTimeMillis);
		System.out.println("Computed 1: " + computed.get());
		System.out.println("Computed 2: " + computed.get());

		// Demonstrate cached value
		Value cached = Values.cached("random", domain, Math::random);
		System.out.println("Cached 1: " + cached.get());
		System.out.println("Cached 2: " + cached.get()); // Same value
	}

	private static void demonstrateHierarchy(Domain root, ProtocolDomain ipv4) {
		// Check value existence
		System.out.println("Contains 'headers/type': " +
				ipv4.getHeaders().containsValue("type"));

		// List values in headers folder
		System.out.println("Header values:");
		ipv4.getHeaders().listValues().forEach(v -> System.out.println("  " + v.name() + " = " + v.get()));

		// Demonstrate folder operations
		System.out.println("\nFolders in ipv4:");
		ipv4.listFolders(DomainPath.of("")).forEach(f -> System.out.println("  " + f.name()));

		// Access nested value
		Value type = ipv4.getHeaders().value("type");
		System.out.println("\nHeader type value: " + type.get());
	}

	private static void demonstrateThreadSafety(ProtocolDomain domain) {
		Value sharedCounter = Values.of("shared", domain, 0);
		int numThreads = 5;
		int incrementsPerThread = 1000;

		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch finishLatch = new CountDownLatch(numThreads);

		// Create threads
		Thread[] threads = new Thread[numThreads];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(() -> {
				try {
					startLatch.await();

					for (int j = 0; j < incrementsPerThread; j++) {
						while (true) {
							Integer current = (Integer) sharedCounter.get();
							if (sharedCounter.compareAndSet(current, current + 1)) {
								break;
							}
							Thread.yield();
						}
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					finishLatch.countDown();
				}
			});
			threads[i].start();
		}

		startLatch.countDown();
		System.out.println("Threads started, waiting for completion...");

		try {
			if (!finishLatch.await(5, TimeUnit.SECONDS)) {
				System.out.println("Warning: Timeout waiting for threads!");
				for (Thread t : threads) {
					if (t.isAlive()) {
						t.interrupt();
					}
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			System.out.println("Main thread interrupted while waiting");
		}

		int finalValue = (Integer) sharedCounter.get();
		int expectedValue = numThreads * incrementsPerThread;
		System.out.printf("Final counter value: %d (expected: %d)%n",
				finalValue, expectedValue);

		if (finalValue != expectedValue) {
			System.out.printf("Warning: Counter mismatch! Missing %d increments%n",
					expectedValue - finalValue);
		}
	}

	private static void demonstrateAdvancedTypes(ProtocolDomain domain) {
		// Demonstrate typed value
		Value typedValue = Values.typed(
				Values.of("port", domain, 80),
				Integer.class);

		System.out.println("Typed value: " + typedValue.get());

		try {
			typedValue.set("invalid");
			System.out.println("Should not reach here");
		} catch (ClassCastException e) {
			System.out.println("Type check prevented invalid assignment (expected)");
		}

		// Demonstrate supplier with validation
		Supplier<Integer> validatedSupplier = () -> {
			int value = (int) (Math.random() * 65536);
			if (value < 0 || value > 65535) {
				throw new IllegalStateException("Port number out of range");
			}
			return value;
		};

		Value portValue = Values.computed("dynamic-port", domain, validatedSupplier);
		System.out.println("Dynamic port: " + portValue.get());
	}
}