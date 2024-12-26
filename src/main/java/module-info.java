/*
 * Sly Technologies Free License
 * 
 * Copyright 2023-2024 Sly Technologies Inc.
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
/**
 * Net modules runtime utilties and APIs
 */
module com.slytechs.jnet.platform.api {

	/* Public API */
	exports com.slytechs.jnet.platform.api;
	exports com.slytechs.jnet.platform.api.common;
	exports com.slytechs.jnet.platform.api.common.binding;
	exports com.slytechs.jnet.platform.api.common.settings;
	exports com.slytechs.jnet.platform.api.util;
	exports com.slytechs.jnet.platform.api.util.time;
	exports com.slytechs.jnet.platform.api.util.hash;
	exports com.slytechs.jnet.platform.api.util.array;
	exports com.slytechs.jnet.platform.api.data;
	exports com.slytechs.jnet.platform.api.data.common.processor;
	exports com.slytechs.jnet.platform.api.data.event;
	exports com.slytechs.jnet.platform.api.data.handler;
	exports com.slytechs.jnet.platform.api.data.pipeline;
	exports com.slytechs.jnet.platform.api.data.pipeline.transform;
	exports com.slytechs.jnet.platform.api.data.pipeline.processor;
	exports com.slytechs.jnet.platform.api.frame;
	exports com.slytechs.jnet.platform.api.vm;

	/* Private API */
	exports com.slytechs.jnet.platform.api.memory.foreign
			to com.slytechs.jnet.jnetntapi.api, // jnetntapi-api
			com.slytechs.jnet.jnetdpdk.api, // jnetdpdk-api
			com.slytechs.jnet.protocol.api; // protocol-api

	exports com.slytechs.jnet.platform.api.memory.foreign.struct
			to com.slytechs.jnet.protocol.api; // protocol-api

	exports com.slytechs.jnet.platform.api.memory.layout
			to com.slytechs.jnet.jnetntapi.api, // jnetntapi-api
			com.slytechs.jnet.protocol.api; // protocol-api

	exports com.slytechs.jnet.platform.api.util.concurrent
			to com.slytechs.jnet.protocol.api; // protocol-api

	exports com.slytechs.jnet.platform.api.util.format
			to com.slytechs.jnet.protocol.api; // protocol-api

	exports com.slytechs.jnet.platform.api.util.function
			to com.slytechs.jnet.jnetpcap.api, // jnetpcap-api
			com.slytechs.jnet.jnetntapi.api, // jnetntapi-api
			com.slytechs.jnet.jnetdpdk.api, // jnetdpdk-api
			com.slytechs.jnet.protocol.api; // protocol-api

	exports com.slytechs.jnet.platform.api.util.collection
			to com.slytechs.jnet.protocol.api; // protocol-api

	exports com.slytechs.jnet.platform.api.util.json
			to com.slytechs.jnet.protocol.api; // protocol-api

	exports com.slytechs.jnet.platform.api.common.impl // For Benchmark class
			to com.slytechs.jnet.protocol.api; // protocol-api

	requires java.logging;

}