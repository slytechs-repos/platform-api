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
package com.slytechs.jnet.platform.api.util.format;

import java.io.IOException;

/**
 * A constant which specifies the detail level to generate by various utility
 * methods.
 * 
 * <p>
 * When applied to <code>PacketFormat(Detail detail)</code>:
 * </p>
 * 
 * <pre>
 *  packet = high
 *    header = high
 *    field = high
 *    
 *  packet = Medium
 *    header = Low
 *    field = None
 *    
 *  packet = Low
 *    header = None
 *    field = None
 *    
 *  header = high
 *    field = high
 *    
 *  header = medium
 *    field = medium
 *    
 *  header = low
 *    field = none
 * </pre>
 *
 * @author Mark Bednarczyk
 */
public enum Detail {

	/** Do not display. */
	OFF,

	/** The summary detail. */
	SUMMARY,

	/** The medium detail. */
	MEDIUM,

	/** The high detail. */
	HIGH,

	/** The debug level detail. */
	DEBUG,

	/** The hexdump level detail. */
	HEXDUMP,;

	/** The Constant DEFAULT. */
	public static final Detail DEFAULT = HIGH;

	/**
	 * Checks if is none.
	 *
	 * @return true, if is none
	 */
	public boolean isOff() {
		return compareTo(OFF) == 0;
	}

	/**
	 * Checks if is low.
	 *
	 * @return true, if is low
	 */
	public boolean isLow() {
		return compareTo(SUMMARY) == 0;
	}

	/**
	 * Checks if is medium.
	 *
	 * @return true, if is medium
	 */
	public boolean isMedium() {
		return compareTo(MEDIUM) == 0;
	}

	/**
	 * Checks if is high.
	 *
	 * @return true, if is high
	 */
	public boolean isHigh() {
		return compareTo(HIGH) == 0;
	}

	/**
	 * Checks if is debug.
	 *
	 * @return true, if is debug
	 */
	public boolean isDebug() {
		return compareTo(DEBUG) == 0;
	}

	/**
	 * Checks if is debug.
	 *
	 * @return true, if is debug
	 */
	public boolean isTrace() {
		return compareTo(HEXDUMP) == 0;
	}

	public void printToString(Printable src) {
		src.printToString(this);
	}

	public void printTo(Appendable out, Printable src) throws IOException {
		src.printTo(out, this);
	}

	public void printToFormatted(Appendable out, DetailFormatter formatter, Printable src) throws IOException {
		src.printFormattedTo(out, formatter, this);
	}

	public void printToStdout(Printable src) {
		src.printToStdout(this);
	}

	public void printToStderr(Printable src) {
		src.printToStderr(this);
	}

	public void printlnToStdout(Printable src) {
		src.printlnToStdout(this);
	}

	public void printlnToStderr(Printable src) {
		src.printlnToStderr(this);
	}

}