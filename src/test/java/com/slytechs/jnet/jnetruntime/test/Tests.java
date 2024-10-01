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
package com.slytechs.jnet.jnetruntime.test;

import java.io.PrintStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.TestInfo;

/**
 * Utility jUnit test methods.
 * 
 */
public final class Tests {

	/** The Constant VERBOSE. */
	public static final boolean VERBOSE = false;
	
	/** The Constant out. */
	public static final PrintStream out = VERBOSE
			? System.out
			: new PrintStream(PrintStream.nullOutputStream());

	/** The Constant err. */
	public static final PrintStream err = VERBOSE
			? System.err
			: new PrintStream(PrintStream.nullOutputStream());

	/**
	 * Enable console logging at specified level.
	 *
	 * @param level the level
	 */
	public static void enableConsoleLogging(Level level) {
		ConsoleHandler consolehandler = new ConsoleHandler();

		Logger anonymouslogger = Logger.getLogger("");
		for (int i = 0; i < anonymouslogger.getHandlers().length; i++) {
			anonymouslogger.removeHandler(anonymouslogger.getHandlers()[i]);
		}
		consolehandler.setLevel(level);
		anonymouslogger.addHandler(consolehandler);
		anonymouslogger.setUseParentHandlers(false);
		anonymouslogger.setLevel(level);
	}

	/**
	 * Display test name.
	 *
	 * @param info the info
	 */
	public static void displayTestName(TestInfo info) {
		Tests.out.printf("---- %s ----%n", info.getDisplayName());
	}

	/**
	 * Instantiates a new tests.
	 */
	private Tests() {
	}

}
