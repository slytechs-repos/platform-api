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

import java.util.Objects;

/**
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 *
 */
public class AbstractTransformer<T_IN, T_OUT, T_BASE extends Transformer<T_IN, T_OUT, T_BASE>>
		implements Transformer<T_IN, T_OUT, T_BASE> {

	private T_OUT output;
	private T_IN input;
	private boolean enabled = true;
	private final DataType inputType;
	private final DataType outputType;
	private final String name;

	public AbstractTransformer(String name, T_IN input, DataType inputType, DataType outputType) {
		this.name = name;
		this.input = input;
		this.inputType = inputType;
		this.outputType = outputType;
	}

	public AbstractTransformer(String name, T_IN input, DataType inputType, T_OUT output, DataType outputType) {
		this.name = name;
		this.input = input;
		this.output = output;
		this.inputType = inputType;
		this.outputType = outputType;
	}

	public AbstractTransformer(String name, DataType inputType, T_OUT output, DataType outputType) {
		this.name = name;
		this.inputType = inputType;
		this.output = output;
		this.outputType = outputType;
	}

	@SuppressWarnings("unchecked")
	public AbstractTransformer(String name, DataType inputType, DataType outputType) {
		this.name = name;
		this.inputType = inputType;
		this.outputType = outputType;
		this.input = (T_IN) this;

	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Transformer#getOutput()
	 */
	@Override
	public T_OUT getOutput() {
		return this.output;
	}

	T_OUT setOutput(T_OUT output) {
		this.output = output;

		return output;
	}

	T_IN setInput(T_IN input) {
		this.input = input;

		return input;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Transformer#getInput()
	 */
	@Override
	public T_IN getInput() {
		return this.input;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Transformer#enable(boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T_BASE enable(boolean b) {
		this.enabled = b;

		return (T_BASE) this;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Transformer#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Transformer#outputType()
	 */
	@Override
	public DataType outputType() {
		return this.outputType;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Transformer#inputType()
	 */
	@Override
	public DataType inputType() {
		return this.inputType;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.Transformer#name()
	 */
	@Override
	public String name() {
		return name;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		var in = input == null ? "" : Objects.toIdentityString(input);
		var out = output == null ? "" : Objects.toIdentityString(output);

		return "AbstractTransformer [name=" + name + ", inputType=" + inputType + ", outputType=" + outputType
				+ ", output=" + out + ", input=" + in + "]";
	}

}
