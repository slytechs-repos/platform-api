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
	exports com.slytechs.jnet.platform.api.common.binding;
	exports com.slytechs.jnet.platform.api.common.settings;
	exports com.slytechs.jnet.platform.api.time;
	exports com.slytechs.jnet.platform.api.frame;
	exports com.slytechs.jnet.platform.api.hash;
	exports com.slytechs.jnet.platform.api.util;
	exports com.slytechs.jnet.platform.api.util.config;
	exports com.slytechs.jnet.platform.api.function;
	exports com.slytechs.jnet.platform.api.data;
	exports com.slytechs.jnet.platform.api.data.event;
	exports com.slytechs.jnet.platform.api.data.handler;
	exports com.slytechs.jnet.platform.api.data.common.processor;
	exports com.slytechs.jnet.platform.api.data.pipeline;
	exports com.slytechs.jnet.platform.api.data.pipeline.transform;
	exports com.slytechs.jnet.platform.api.data.pipeline.processor;
	exports com.slytechs.jnet.platform.api.vm;

	/* Private API */
	exports com.slytechs.jnet.platform.api.memory.foreign;
	exports com.slytechs.jnet.platform.api.memory.foreign.struct;
	
	exports com.slytechs.jnet.platform.api.internal;
	exports com.slytechs.jnet.platform.api.memory.layout;
	exports com.slytechs.jnet.platform.api.internal.concurrent;
	exports com.slytechs.jnet.platform.api.internal.util;
	exports com.slytechs.jnet.platform.api.internal.util.format;
	exports com.slytechs.jnet.platform.api.internal.util.function;
	exports com.slytechs.jnet.platform.api.internal.util.collection;
	exports com.slytechs.jnet.platform.api.internal.json;

	requires java.logging;

}