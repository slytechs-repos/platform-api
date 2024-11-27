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
package com.slytechs.jnet.jnetruntime.util;

import com.slytechs.jnet.jnetruntime.util.Settings.Property;
import com.slytechs.jnet.jnetruntime.util.Settings.Property.Action;
import com.slytechs.jnet.jnetruntime.util.Settings.StringProperty;

/**
 * @author Mark Bednarczyk [mark@slytechs.com]
 * @author Sly Technologies Inc.
 */
class TestSettings {

	static class MySettings extends Settings {

		private final IntProperty x = ofInt("property.x", 10, Action.ofAction(this::setX, this).andThen(this::setY)).systemProperty();

		MySettings() {

		}

		public void setX(int newValue, Object source) {
			if (source != this)
				x.setValue(newValue, this);
			System.out.println("mySettings::setX x=" + newValue + ", source=" + source);
		}

		public void setY(int newValue) {
			System.out.println("mySettings::setY x=" + newValue);
		}

		public int getX() {
			return x.getInt();
		}
	}

	public static void main(String[] args) {

		StringProperty property = Property.of("property.id", 10)
				.on(System.out::println)
//				.setValue(UnsignedIntProperty.MAX_VALUE + 1)
//				.setUnsignedInt("-1")
//				.setUnsignedInt(-1)
//				.setValue(10l)
				.setFormat("0x%08X")
//				.map(i -> String.valueOf(i))
				.mapToFormattedString()

		;

		System.out.println(property);

		new MySettings()
				.setX(20, null);;
	}
}
