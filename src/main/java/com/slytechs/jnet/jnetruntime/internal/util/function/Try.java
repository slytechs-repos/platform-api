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
package com.slytechs.jnet.jnetruntime.internal.util.function;

/**
 * The Class Try.
 *
 * @param <R> the generic type
 * @author Mark Bednarczyk
 */
public class Try<R> {
	
	/** The failure. */
	private final Exception failure;
	
	/** The succes. */
	private final R succes;

	/**
	 * Instantiates a new try.
	 *
	 * @param failure the failure
	 * @param succes  the succes
	 */
	public Try(Exception failure, R succes) {
		this.failure = failure;
		this.succes = succes;
	}
}
