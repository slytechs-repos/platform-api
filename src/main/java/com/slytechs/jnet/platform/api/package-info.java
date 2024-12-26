/**
 * Platform API for high-performance network packet processing and memory
 * management.
 * <p>
 * This package hierarchy provides:
 * <ul>
 * <li><b>Memory Management</b> - Native/foreign memory, layouts, and buffer
 * operations</li>
 * <li><b>Data Processing</b> - Pipelines, actors, batching for efficient packet
 * handling</li>
 * <li><b>Common Services</b> - Bindings, settings, and core platform
 * functionality</li>
 * <li><b>Utilities</b> - Collections, concurrency, formatting, and general
 * helpers</li>
 * </ul>
 * 
 * <h2>Key Package Groups</h2>
 * <ul>
 * <li>{@code memory.*} - Memory management subsystem</li>
 * <li>{@code data.*} - Data processing framework</li>
 * <li>{@code common.*} - Core platform services</li>
 * <li>{@code util.*} - Support utilities</li>
 * </ul>
 * 
 * <h2>Platform Design</h2> The API is designed for:
 * <ul>
 * <li>High performance packet processing</li>
 * <li>Zero-copy operations where possible</li>
 * <li>Efficient memory management</li>
 * <li>Flexible processing pipelines</li>
 * <li>Modular, extensible architecture</li>
 * </ul>
 * 
 * @see com.slytechs.jnet.platform.api.memory
 * @see com.slytechs.jnet.platform.api.data
 * @see com.slytechs.jnet.platform.api.common
 * @see com.slytechs.jnet.platform.api.util
 *
 * @since 1.0
 */
package com.slytechs.jnet.platform.api;
