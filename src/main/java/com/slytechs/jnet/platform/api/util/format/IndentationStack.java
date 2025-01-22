/*
 * Sly Technologies Free License
 * 
 * Copyright 2025 Sly Technologies Inc.
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
import java.util.Stack;

public class IndentationStack implements Indentation {
	private boolean dirty;
	private final Stack<String> stack = new Stack<>();

	public IndentationStack() {
		stack.push("");
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.format.Indentation#markDirty()
	 */
	@Override
	public void markDirty() {
		this.dirty = true;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.format.Indentation#clearDirty()
	 */
	@Override
	public void clearDirty() {
		this.dirty = false;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.format.Indentation#push(int)
	 */
	@Override
	public void push(int spaceCount) {
		stack.push(peek() + " ".repeat(spaceCount));
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.format.Indentation#peek()
	 */
	@Override
	public String peek() {
		return stack.peek();
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.format.Indentation#pop()
	 */
	@Override
	public void pop() {
		stack.pop();
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.format.Indentation#indent(Appendable)
	 */
	@Override
	public Appendable indent(Appendable out) throws IOException {
		if (!dirty)
			out.append(peek());

		return out;
	}

	/**
	 * @see com.slytechs.jnet.platform.api.util.format.Indentation#pushAndPrint(Appendable,
	 *      java.lang.String)
	 */
	@Override
	public void pushAndPrint(Appendable out, String str) throws IOException {
		indent(out).append(str);
		push(str.length());
		dirty = true;
	}
}