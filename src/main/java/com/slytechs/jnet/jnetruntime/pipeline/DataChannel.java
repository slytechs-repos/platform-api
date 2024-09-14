package com.slytechs.jnet.jnetruntime.pipeline;

import java.util.List;

import com.slytechs.jnet.jnetruntime.util.Reconfigurable;
import com.slytechs.jnet.jnetruntime.util.Registration;

/**
 * The {@code DataChannel} interface represents a data channel that manages a
 * collection of {@link ChannelNode}s, processes varied multiple inputs, and
 * maps them to custom data type outputs. It allows adding inputs, processors,
 * mappers, and outputs with custom data types, while supporting
 * reconfiguration.
 *
 * This interface is reconfigurable via the {@link Reconfigurable} interface and
 * offers methods to add and manage data inputs, mappers, outputs, and
 * processors with prioritization. Additionally, it supports retrieval and
 * listing of inputs, processors, and mappers based on different criteria.
 *
 * <p>
 * Each channel is associated with a particular {@link DataType}, and it
 * supports the dynamic addition and management of processors and mappers at
 * different priority levels.
 * </p>
 *
 * @param <T> the type of the {@link DataProcessor} used by the channel.
 * @param <C> the custom data type or context passed through the data
 *            processors.
 *
 * @see ChannelNode
 * @see Reconfigurable
 * @see DataProcessor
 * @see ChannelInput
 * @see DataMapper
 * @see Registration
 * @see DataType
 * 
 * @author Sly Technologies Inc
 * @since 2024
 */
public interface DataChannel<T extends DataProcessor<T, C>, C> extends ChannelNode, Reconfigurable {

	/**
	 * Adds a new input to the data channel.
	 *
	 * @param <T_IN> the type of the input data
	 * @param input  the input to be added, represented as a {@link ChannelInput}
	 * @param type   the {@link DataType} of the input data
	 * @return a {@link Registration} object for the input
	 */
	<T_IN> Registration addInput(ChannelInput<T_IN, T, C> input, DataType type);

	/**
	 * Adds a data mapper to the data channel.
	 *
	 * @param <T_OUT> the type of the output data from the mapper
	 * @param mapper  the mapper to be added, represented as a {@link DataMapper}
	 * @param type    the {@link DataType} of the output data
	 * @return a {@link Registration} object for the mapper
	 */
	<T_OUT> Registration addMapper(DataMapper<T, T_OUT, C> mapper, DataType type);

	/**
	 * Adds an output to the data channel.
	 *
	 * @param <T_OUT> the type of the output data
	 * @param output  the output to be added
	 * @param type    the {@link DataType} of the output data
	 * @return a {@link Registration} object for the output
	 */
	<T_OUT> Registration addOutput(T_OUT output, DataType type);

	/**
	 * Adds a data processor to the channel with a given priority.
	 *
	 * @param priority  the priority level of the processor
	 * @param processor the processor to be added, represented as a
	 *                  {@link DataProcessor}
	 * @return a {@link Registration} object for the processor
	 */
	Registration addProcessor(int priority, T processor);

	/**
	 * Adds a data processor to the channel with a given priority and a custom name.
	 *
	 * @param priority  the priority level of the processor
	 * @param processor the processor to be added
	 * @param name      the custom name for the processor
	 * @return a {@link Registration} object for the processor
	 */
	Registration addProcessor(int priority, T processor, String name);

	/**
	 * Retrieves the data type associated with this channel.
	 *
	 * @return the {@link DataType} of this channel
	 */
	DataType channelType();

	/**
	 * Retrieves an input from the channel by its data type.
	 *
	 * @param <T_IN> the type of the input data
	 * @param type   the {@link DataType} of the input
	 * @return the input associated with the specified data type
	 */
	<T_IN> T_IN getInput(DataType type);

	/**
	 * Retrieves an output from the channel by its data type.
	 *
	 * @param <T_OUT> the type of the output data
	 * @param type    the {@link DataType} of the output
	 * @return the output associated with the specified data type
	 */
	<T_OUT> T_OUT getOutput(DataType type);

	/**
	 * Retrieves a processor from the channel based on its priority and class type.
	 *
	 * @param <P>            the type of the processor
	 * @param priority       the priority level of the processor
	 * @param processorClass the class of the processor
	 * @return the processor instance of the specified class and priority
	 */
	<P extends T> P getProcessor(int priority, Class<P> processorClass);

	/**
	 * Retrieves a processor from the channel based on its name and class type.
	 *
	 * @param <P>            the type of the processor
	 * @param name           the custom name of the processor
	 * @param processorClass the class of the processor
	 * @return the processor instance of the specified class and name
	 */
	<P extends T> P getProcessor(String name, Class<P> processorClass);

	/**
	 * Lists all the inputs added to this channel.
	 *
	 * @return a {@link List} of {@link ChannelInput} objects
	 */
	List<ChannelInput<?, T, C>> listInputs();

	/**
	 * Lists all the data mappers added to this channel.
	 *
	 * @return a {@link List} of {@link DataMapper} objects
	 */
	List<DataMapper<T, ?, C>> listMappers();

	/**
	 * Lists all the outputs added to this channel.
	 *
	 * @return a {@link List} of outputs
	 */
	List<?> listOutputs();

	/**
	 * Lists all the data processors added to this channel.
	 *
	 * @return a {@link List} of {@link DataProcessor} objects
	 */
	List<T> listProcessors();

}
