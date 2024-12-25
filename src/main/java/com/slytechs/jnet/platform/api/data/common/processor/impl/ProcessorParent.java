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
package com.slytechs.jnet.platform.api.data.common.processor.impl;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import com.slytechs.jnet.platform.api.data.DataType;

/**
 * 
 *
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface ProcessorParent<T> {

	ReadWriteLock getRwLock();

	default Lock getReadLock() {
		return getRwLock().readLock();
	}

	default Lock getWriteLock() {
		return getRwLock().writeLock();
	}

	void onEnableProcessor(ProcessorBase<T> sourceProcessor, boolean newState);
	
	DataType<T> dataType();
}
