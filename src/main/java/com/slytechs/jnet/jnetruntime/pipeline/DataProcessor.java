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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import com.slytechs.jnet.jnetruntime.NotFound;
import com.slytechs.jnet.jnetruntime.util.HasName;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * Represents a data processor in a pipeline, capable of processing input data
 * and producing output data.
 *
 * @param <T>      The type of data processed by this processor
 * @param <T_BASE> The specific type of the processor implementation
 * @author Mark Bednarczyk
 */
public interface DataProcessor<T, T_BASE extends DataProcessor<T, T_BASE>>
		extends HasName, PipelineNode<T_BASE>, HasOutputData<T>, HasInputData<T> {

	/**
	 * Listener interface for data change events.
	 *
	 * @param <T> The type of data being processed
	 * @see DataChangeEvent
	 */
	interface DataChangeListener<T> {
		/**
		 * Called when the data changes.
		 *
		 * @param newData The new data
		 */
		void onDataChange(T newData);
	}

	/**
	 * Supports data change notifications for processors.
	 *
	 * @param <T> The type of data being processed
	 * @author Mark Bednarczyk
	 */
	public class DataChangeSupport<T> {

		/** The listener list. */
		private final List<DataChangeListener<T>> listenerList = new ArrayList<>();

		/**
		 * Adds a data change listener.
		 *
		 * @param listener The listener to add
		 * @return A registration object for the added listener
		 */
		public Registration addListener(DataChangeListener<T> listener) {
			listenerList.add(listener);
			return () -> listenerList.remove(listener);
		}

		/**
		 * Dispatches a data change event to all registered listeners.
		 *
		 * @param newData The new data to dispatch
		 */
		public void dispatch(T newData) {
			listenerList.forEach(l -> l.onDataChange(newData));
		}
	}

	/**
	 * Represents a bypassable component in the pipeline.
	 *
	 * @param <T>      The type of data processed by this component
	 * @param <T_BASE> The specific type of the bypassable component implementation
	 * @author Mark Bednarczyk
	 */
	interface IsBypassable<T, T_BASE extends IsBypassable<T, T_BASE>> {

		/**
		 * Sets the bypass state of this component.
		 *
		 * @param b true to bypass, false otherwise
		 * @return This component instance
		 */
		T_BASE bypass(boolean b);

		/**
		 * Sets the bypass state of this component using a boolean supplier.
		 *
		 * @param b A supplier that determines whether to bypass
		 * @return This component instance
		 */
		T_BASE bypass(BooleanSupplier b);

		/**
		 * Sets the bypass data for this component.
		 *
		 * @param bypassData The bypass data to set
		 * @return This component instance
		 */
		T_BASE bypass(T bypassData);

		/**
		 * Gets the bypass data for this component.
		 *
		 * @return The bypass data
		 */
		T bypassData();

		/**
		 * Checks if this component is currently bypassed.
		 *
		 * @return true if bypassed, false otherwise
		 */
		default boolean isBypassed() {
			return bypassData() != null;
		}
	}

	/**
	 * Factory for creating data processors.
	 *
	 * @param <T>      The type of data processed by the processor
	 * @param <T_BASE> The specific type of the processor implementation
	 */
	interface ProcessorFactory<T, T_BASE extends DataProcessor<T, T_BASE>> {
		
		interface Builder1Arg<T, T1, T_BASE extends DataProcessor<T, T_BASE>> {
			ProcessorFactory<T, T_BASE> newFactory(T1 arg1) throws NotFound;
		}
		
		interface Builder2Args<T, T1, T2, T_BASE extends DataProcessor<T, T_BASE>> {
			ProcessorFactory<T, T_BASE> newFactory(T1 arg1, T2 arg2);
		}
		
		/**
		 * Factory for creating named data processors.
		 *
		 * @param <T>      The type of data processed by the processor
		 * @param <T_BASE> The specific type of the processor implementation
		 * @author Mark Bednarczyk
		 */
		interface Named<T, T_BASE extends DataProcessor<T, T_BASE>> {
			/**
			 * Creates a new named processor instance.
			 *
			 * @param parent   The parent pipeline
			 * @param priority The priority of the processor
			 * @param name     The name of the processor
			 * @return A new processor instance
			 */
			T_BASE newProcessor(Pipeline<T, ?> parent, int priority, String name);
		}

		default DataType dataType() {
			throw new UnsupportedOperationException("missing builder implementation");
		}
		
		/**
		 * Creates a new processor instance.
		 *
		 * @param parent   The parent pipeline
		 * @param priority The priority of the processor
		 * @return A new processor instance
		 */
		T_BASE newProcessor(Pipeline<T, ?> parent, int priority);
	}

	/**
	 * Gets the data type processed by this processor.
	 *
	 * @return The data type
	 */
	DataType dataType();

	/**
	 * {@inheritDoc}
	 */
	@Override
	T inputData();

	/**
	 * Gets the priority of this processor.
	 *
	 * @return The priority
	 */
	int priority();

	/**
	 * Sets the priority of this processor.
	 *
	 * @param newPriority The new priority to set
	 * @return This processor instance
	 */
	T_BASE priority(int newPriority);
}