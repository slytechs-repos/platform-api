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
package com.slytechs.jnet.platform.api.domain.spi;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;

import com.slytechs.jnet.platform.api.domain.Domain;
import com.slytechs.jnet.platform.api.domain.DomainPath;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
public interface DomainService {

	interface DomainExport {
		default String moduleName() {
			return getClass().getModule().getName();
		}

		DomainPath exportPath();

		Domain exportDomain();

		default BiConsumer<DomainPath, Domain> notifyOnExportListener() {
			return (p, d) -> {};
		}
	}

	static List<DomainExport> mergeDomains() {
		var merged = new ArrayList<DomainExport>();

		ServiceLoader.load(DomainService.class).stream()
				.forEach(s -> merged.addAll(s.get().listDomains()));

		return merged;
	}

	List<DomainExport> listDomains();

}
