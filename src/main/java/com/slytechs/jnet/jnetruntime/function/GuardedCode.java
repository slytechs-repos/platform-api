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
package com.slytechs.jnet.jnetruntime.function;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;

/**
 * 
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public class GuardedCode {

	@SuppressWarnings("unused")
	private final ReadWriteLock rwLock;
	private final Lock readLock;
	private final Lock writeLock;
	private final Consumer<Throwable> exceptionHandler;

	public GuardedCode(ReadWriteLock rwLock) {
		this(rwLock, Throwable::printStackTrace);

	}

	public GuardedCode(ReadWriteLock rwLock, Consumer<Throwable> exceptionHandler) {
		this.rwLock = rwLock;
		this.exceptionHandler = exceptionHandler;
		this.readLock = rwLock.readLock();
		this.writeLock = rwLock.writeLock();
	}

	public void readLockedVoid(Runnable code) {
		readLock.lock();

		try {
			code.run();
		} finally {
			readLock.unlock();
		}
	}

	public void writeLockedVoid(Runnable code) {
		writeLock.lock();

		try {
			code.run();
		} finally {
			writeLock.unlock();
		}
	}

	public <T, E extends Throwable> T readLocked(Callable<T> code, Class<E> exceptionClass) throws E {
		readLock.lock();

		try {
			return code.call();
		} catch (Throwable e) {
			if (exceptionClass.isAssignableFrom(e.getClass()))
				throw (E) e;

			exceptionHandler.accept(e);

			return null;
		} finally {
			readLock.unlock();
		}
	}

	public <T, E extends Throwable> T writeLocked(Callable<T> code, Class<E> exceptionClass) throws E {
		writeLock.lock();

		try {
			return code.call();
		} catch (Throwable e) {
			if (exceptionClass.isAssignableFrom(e.getClass()))
				throw (E) e;

			exceptionHandler.accept(e);

			return null;
		} finally {
			writeLock.unlock();
		}
	}

	public <T> T readLocked(Callable<T> code) {
		readLock.lock();

		try {
			return code.call();
		} catch (Exception e) {
			exceptionHandler.accept(e);

			return null;
		} finally {
			readLock.unlock();
		}
	}

	public <T> T writeLocked(Callable<T> code) {
		writeLock.lock();

		try {
			return code.call();
		} catch (Exception e) {
			exceptionHandler.accept(e);

			return null;
		} finally {
			writeLock.unlock();
		}
	}

}
