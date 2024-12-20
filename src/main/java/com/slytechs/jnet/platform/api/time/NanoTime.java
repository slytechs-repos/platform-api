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
package com.slytechs.jnet.platform.api.time;

import java.util.concurrent.TimeUnit;

/**
 * Provides high-precision timing utilities for managing delays and time-based
 * operations at nanosecond resolution. This class combines both blocking and
 * spinning delay mechanisms to achieve more accurate timing control than
 * traditional sleep operations.
 * 
 * <p>
 * The class implements a hybrid delay strategy where longer delays use blocking
 * operations for efficiency, while shorter delays use CPU spinning for
 * precision. This approach helps minimize both CPU usage and timing
 * inaccuracies.
 * </p>
 *
 * <p>
 * Note that actual timing precision may vary based on system capabilities, CPU
 * load, and operating system scheduling.
 * </p>
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class NanoTime {

	/** Constant for converting milliseconds to nanoseconds */
	private static long NANOS_IN_MILLIS = 1_000_000;

	/**
	 * Returns the current epoch time in nanoseconds, representing nanoseconds
	 * elapsed since the Unix epoch (January 1, 1970 00:00:00 UTC).
	 * 
	 * <p>
	 * This method combines the wall-clock time from
	 * {@link System#currentTimeMillis()} with sub-millisecond precision from
	 * {@link #nanoTime()} to provide nanosecond resolution timestamps relative to
	 * epoch. The calculation:
	 * <ol>
	 * <li>Gets the epoch time in milliseconds</li>
	 * <li>Gets the fractional nanoseconds within the current millisecond</li>
	 * <li>Combines them to create a complete nanosecond-precision epoch
	 * timestamp</li>
	 * </ol>
	 * </p>
	 *
	 * <p>
	 * <b>Note:</b> While this provides nanosecond resolution, the absolute accuracy
	 * is limited by the underlying system clock synchronization and the factors
	 * that affect nanosecond precision timing.
	 * </p>
	 *
	 * @return nanoseconds since the Unix epoch (1970-01-01T00:00:00Z)
	 * @see System#currentTimeMillis()
	 * @see #nanoTime()
	 */
	public static long currentTimeNanos() {
		long epocMillis = System.currentTimeMillis();
		long nanos = nanoTime();

		long epocNanos = epocMillis * NANOS_IN_MILLIS + (nanos % NANOS_IN_MILLIS);

		return epocNanos;
	}

	/**
	 * Returns the current epoch time in nanoseconds, representing nanoseconds
	 * elapsed since the Unix epoch (January 1, 1970 00:00:00 UTC).
	 * 
	 * <p>
	 * This method combines the wall-clock time from
	 * {@link System#currentTimeMillis()} with sub-millisecond precision from
	 * {@link #nanoTime()} to provide nanosecond resolution timestamps relative to
	 * epoch. The calculation:
	 * <ol>
	 * <li>Gets the epoch time in milliseconds</li>
	 * <li>Gets the fractional nanoseconds within the current millisecond</li>
	 * <li>Combines them to create a complete nanosecond-precision epoch
	 * timestamp</li>
	 * </ol>
	 * </p>
	 *
	 * <p>
	 * <b>Note:</b> While this provides nanosecond resolution, the absolute accuracy
	 * is limited by the underlying system clock synchronization and the factors
	 * that affect nanosecond precision timing.
	 * </p>
	 *
	 * @return nanoseconds since the Unix epoch (1970-01-01T00:00:00Z)
	 * @see System#currentTimeMillis()
	 * @see #nanoTime()
	 */
	public static long offsetNanoTime(long offsetFrom, long delta) {
		return offsetFrom + delta;
	}

	public static long deltaFromStart(long start) {
		return currentTimeNanos() - start;
	}

	/**
	 * Returns the current value of the most precise available system timer, in
	 * nanoseconds. When available, specialized hardware timing sources may be used
	 * to provide higher precision than the standard system timer. Current and
	 * future implementations may include support for:
	 * 
	 * <ul>
	 * <li>FPGA-based precision timing modules</li>
	 * <li>Napatech Network Adapter hardware timestamps</li>
	 * <li>Specialized timing hardware</li>
	 * <li>System high-precision timer ({@link System#nanoTime()})</li>
	 * </ul>
	 *
	 * <p>
	 * <b>Note on timing precision:</b> At nanosecond precision, timing accuracy is
	 * highly susceptible to various environmental and system factors including but
	 * not limited to:
	 * <ul>
	 * <li>Ambient temperature fluctuations</li>
	 * <li>System load and scheduling</li>
	 * <li>CPU frequency scaling</li>
	 * <li>Hardware thermal throttling</li>
	 * <li>Power management states</li>
	 * </ul>
	 * </p>
	 *
	 * @return the current value from the highest precision available timer source,
	 *         in nanoseconds
	 * @see System#nanoTime()
	 */
	public static long nanoTime() {
		return System.nanoTime();
	}

	/**
	 * Introduces a delay of specified duration using a combination of blocking and
	 * spinning strategies for improved timing accuracy.
	 *
	 * @param duration the length of time to delay
	 * @param unit     the time unit of the duration parameter
	 * @throws InterruptedException if the thread is interrupted while waiting
	 * @see TimeUnit
	 */
	public static void delay(long duration, TimeUnit unit) throws InterruptedException {
		delay(unit.toNanos(duration));
	}

	/**
	 * Implements a hybrid delay mechanism that attempts to maintain a minimum
	 * inter-frame gap specified in nanoseconds. The method combines both blocking
	 * and spinning delays to achieve better timing accuracy while managing CPU
	 * usage.
	 * 
	 * <p>
	 * The delay mechanism works as follows:
	 * </p>
	 * <ul>
	 * <li>For longer delays (>150ms), uses blocking delay to conserve CPU
	 * resources</li>
	 * <li>For shorter delays, uses spin-waiting to achieve higher precision</li>
	 * <li>Accounts for system timing resolution (approximately 35μs) in
	 * calculations</li>
	 * </ul>
	 *
	 * @param delayInNanos the minimum inter-frame gap in nanoseconds to maintain
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	public static void delay(long delayInNanos) throws InterruptedException {

//		System.out.printf("NanoTime::delay delayInNanos=%,dns%n", delayInNanos);

		final long MIN_BLOCK = 150_000_000;
		final long MAX_RESOLUTION = 0;
		long start = System.nanoTime();
		long end = start + delayInNanos - MAX_RESOLUTION;

		long remaining = end - start;
		do {
			remaining = end - System.nanoTime();

			if (remaining > MIN_BLOCK)
				delayBlock(end);
			else
				delaySpin(end);

		} while (remaining > 0);

		long actual = System.nanoTime() - start;

//		System.out.printf("NanoTime::delay actual=%,dns%n", actual);
	}

	/**
	 * Implements a spin-wait delay mechanism that runs until a specified end time
	 * is reached. This method provides high-precision timing but uses active CPU
	 * cycles to achieve it.
	 * 
	 * <p>
	 * Spin-waiting is typically used for very short delays where the overhead of
	 * thread scheduling would be significant compared to the desired delay time.
	 * However, it should be used carefully as it consumes CPU resources actively.
	 * </p>
	 *
	 * @param endNanoTime the absolute time in nanoseconds at which the delay should
	 *                    end, as obtained from {@link System#nanoTime()}
	 * @throws InterruptedException if the thread is interrupted during the
	 *                              spin-wait
	 */
	public static void delaySpin(long endNanoTime) throws InterruptedException {
//		System.out.printf("NanoTime::delaySpin duration=%,dns%n", endNanoTime - System.nanoTime() );
		while (System.nanoTime() < endNanoTime)
			if (Thread.currentThread().isInterrupted())
				throw new InterruptedException();
	}

	/**
	 * Implements a blocking delay mechanism that sleeps until a specified end time
	 * is reached. This method is more CPU-efficient than spin-waiting but may be
	 * less precise due to thread scheduling overhead.
	 * 
	 * <p>
	 * The method uses {@link TimeUnit#NANOSECONDS} for sleeping, which provides the
	 * best available precision for thread sleeping on the platform, though actual
	 * precision may be limited by the operating system's timer resolution.
	 * </p>
	 *
	 * @param endNanoTime the absolute time in nanoseconds at which the delay should
	 *                    end, as obtained from {@link System#nanoTime()}
	 * @throws InterruptedException if the thread is interrupted during sleep
	 */
	public static void delayBlock(long endNanoTime) throws InterruptedException {
//		System.out.printf("NanoTime::delayBlock duration=%,dns%n", endNanoTime - System.nanoTime());
		TimeUnit.NANOSECONDS.sleep(endNanoTime - System.nanoTime());
	}
}
