/**
 * Provides a comprehensive pipeline processing framework for building data processing pipelines
 * with configurable error handling and event notification capabilities. The pipeline architecture
 * supports type-safe data processing with flexible input and output transformations.
 * 
 * <h2>Overview</h2>
 * The pipeline package enables construction of data processing pipelines where data flows through
 * a series of processors. Each processor can transform, filter, or analyze the data before passing
 * it to the next stage. The framework includes built-in support for:
 * <ul>
 *   <li>Type-safe data processing
 *   <li>Priority-based processor ordering
 *   <li>Dynamic enabling/disabling of processors
 *   <li>Thread-safe operations with read-write locks
 *   <li>Input/Output transformations
 * </ul>
 * 
 * <h2>Pipeline Structure</h2>
 * A pipeline consists of three main parts:
 * <ul>
 *   <li><b>Head</b> - Entry point that manages input transformers
 *   <li><b>Processors</b> - Middle stages that process data
 *   <li><b>Tail</b> - Exit point that manages output transformers
 * </ul>
 * 
 * <h2>Processor Types</h2>
 * The framework supports several types of processors:
 * 
 * <h3>1. Default Processors</h3>
 * Created using the pipeline's addProcessor method:
 * <pre>{@code 
 * // Add a simple processor
 * pipeline.addProcessor(priority, "uppercase", sink -> 
 *     data -> sink.get().process(data.toUpperCase())
 * );
 * }</pre>
 * 
 * <h3>2. Custom Processors</h3>
 * Extended from the Processor base class:
 * <pre>{@code 
 * class CustomProcessor<T> extends Processor<T> {
 *     public CustomProcessor(int priority, String name) {
 *         super(priority, name);
 *     }
 *     
 *     // Custom processing logic
 * }
 * }</pre>
 * 
 * <h2>Input/Output Handling</h2>
 * 
 * <h3>Input Transformers</h3>
 * Convert external data types to pipeline's internal type:
 * <pre>{@code 
 * // Register input transformer
 * pipeline.head().<IntConsumer>addInputTransformer("intInput", sink ->
 *     num -> sink.get().accept(Integer.toString(num))
 * );
 * 
 * // Use input
 * IntConsumer input = pipeline.in("intInput");
 * input.accept(42);
 * }</pre>
 * 
 * <h3>Output Transformers</h3>
 * Convert pipeline's internal type to external types:
 * <pre>{@code 
 * // Register output transformer
 * pipeline.tail().<Consumer<String>>addOutputTransformer(0, "stringOutput", sink ->
 *     str -> sink.get().accept(str.toString())
 * );
 * 
 * // Connect output
 * pipeline.out("stringOutput", System.out::println);
 * }</pre>
 * 
 * <h2>Complete Pipeline Example</h2>
 * Here's an example of creating a string processing pipeline:
 * 
 * <pre>{@code 
 * class StringPipeline extends Pipeline<Consumer<StringBuilder>> {
 *     public StringPipeline() {
 *         super("String Pipeline", new StringDataType());
 *         
 *         // Add input transformer for integers
 *         head().<IntConsumer>addInputTransformer("int", sink ->
 *             num -> sink.get().accept(new StringBuilder(Integer.toString(num)))
 *         );
 *         
 *         // Add processors
 *         addProcessor(10, "uppercase", sink ->
 *             str -> sink.get().accept(str.append("-UPPER"))
 *         );
 *         
 *         addProcessor(20, "timestamp", sink ->
 *             str -> sink.get().accept(str.append("-" + System.currentTimeMillis()))
 *         );
 *         
 *         // Add output transformer
 *         tail().<Consumer<String>>addOutputTransformer(0, "string", sink ->
 *             str -> sink.get().accept(str.toString())
 *         );
 *     }
 * }
 * 
 * // Usage
 * StringPipeline pipeline = new StringPipeline();
 * 
 * // Get input and connect output
 * IntConsumer input = pipeline.in("int");
 * pipeline.out("string", System.out::println);
 * 
 * // Process data
 * input.accept(42);
 * }</pre>
 * 
 * <h2>Thread Safety</h2>
 * The pipeline framework uses {@link java.util.concurrent.locks.ReadWriteLock} for thread safety:
 * <ul>
 *   <li>Read locks for data processing operations
 *   <li>Write locks for structural modifications
 *   <li>Each processor shares the pipeline's lock
 * </ul>
 * 
 * <h2>Priority-based Processing</h2>
 * Processors are ordered by priority:
 * <ul>
 *   <li>Lower priority numbers execute first
 *   <li>Processors with same priority maintain addition order
 *   <li>Priority ordering is maintained when enabling/disabling processors
 * </ul>
 * 
 * <h2>Dynamic Configuration</h2>
 * Pipelines support runtime modifications:
 * <ul>
 *   <li>Adding/removing processors
 *   <li>Enabling/disabling processors
 *   <li>Adding/removing input transformers
 *   <li>Adding/removing output transformers
 * </ul>
 * 
 * <h2>Registration and Cleanup</h2>
 * All additions return a {@link com.slytechs.jnet.platform.api.util.Registration} object:
 * <pre>{@code 
 * // Add with cleanup
 * Registration reg = pipeline.addProcessor(...);
 * 
 * // Later cleanup
 * reg.close();
 * }</pre>
 * 
 * <h2>Best Practices</h2>
 * <ol>
 *   <li>Use meaningful processor names for debugging
 *   <li>Choose appropriate priorities for processing order
 *   <li>Properly clean up registrations when no longer needed
 *   <li>Use type-safe input/output transformers
 *   <li>Handle concurrent access appropriately
 *   <li>Monitor pipeline state through registration listeners
 * </ol>
 * 
 * @see com.slytechs.jnet.platform.api.data.pipeline.Pipeline
 * @see com.slytechs.jnet.platform.api.data.common.processor.Processor
 * @see com.slytechs.jnet.platform.api.data.pipeline.processor.Head
 * @see com.slytechs.jnet.platform.api.data.pipeline.processor.Tail
 */
package com.slytechs.jnet.platform.api.data.pipeline;