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
package com.slytechs.jnet.platform.api.data.pipeline;

/**
 * Severity levels for pipeline errors.
 */
public enum ErrorSeverity {
    /** Informational message about a minor issue */
    INFO,
    /** Warning about a potential problem */
    WARNING,
    /** Error that affects operation but pipeline can continue */
    ERROR,
    /** Severe error that requires pipeline shutdown */
    FATAL
}