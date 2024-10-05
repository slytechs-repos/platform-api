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

import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer;
import com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer.EndPoint;
import com.slytechs.jnet.jnetruntime.pipeline.DataType.DataSupport;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * @author Mark Bednarczyk
 *
 */
public class MutableEndPoint<T>
		extends AbstractComponent<MutableEndPoint<T>>
		implements EndPoint<T> {

	/** The id. */
	private final String id;

	private final AbstractOutput<?, T, ?> output;

	private final Registration outputRegistration;

	private final DataProxy<T> dataProxy;

	public MutableEndPoint(OutputTransformer<T> output, String id) {
		super((PipeComponent<?>) output, id, 0);
		this.output = (AbstractOutput<?, T, ?>) output;
		this.id = id;

		DataSupport<T> support = endPointType().dataSupport();
		this.dataProxy = new DataProxy<>(support.dataClass(), null);

		this.outputRegistration = output.registerOutputData(dataProxy.getProxy());
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.util.Registration#unregister()
	 */
	@Override
	public void unregister() {
		try {
			writeLock.lock();

			output.removeEndPoint(this);
			outputRegistration.unregister();

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer.EndPoint#endPointData(java.lang.Object)
	 */
	@Override
	public void endPointData(T data) {
		try {
			System.out.println("endPointData:: rwLock=" + rwLock);
			writeLock.lock();

			dataProxy.setInstance(data);

		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer.EndPoint#id()
	 */
	@Override
	public String id() {
		return id;
	}

	/**
	 * @see com.slytechs.jnet.jnetruntime.pipeline.DataTransformer.OutputTransformer.EndPoint#endPointType()
	 */
	@Override
	public DataType endPointType() {
		return output.outputType();
	}

}
