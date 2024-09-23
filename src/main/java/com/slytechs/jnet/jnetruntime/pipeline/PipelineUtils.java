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
package com.slytechs.jnet.jnetruntime.pipeline;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Utility methods for looking up and computing pipeline elements.
 * 
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
class PipelineUtils {

	private PipelineUtils() {
		/* Do not instantiate */
	}

	public static <T extends Annotation> T lookupAnnotationRecusively(Method method, Class<T> atype) {
		T a = method.getAnnotation(atype);
		if (a == null)
			return lookupAnnotationRecusively(method.getDeclaringClass(), atype);

		return a;
	}

	public static <T extends Annotation> T lookupAnnotationRecusively(Class<?> clazz, Class<T> atype) {
		if (clazz == null)
			return null;
		
		T a = clazz.getAnnotation(atype);
		if (a == null)
			return lookupAnnotationRecusively(clazz.getSuperclass(), atype);

		return a;
	}

	public static DataType lookupDataType(Class<?> typeClass, TypeLookup lookup) {
		if (lookup == null)
			return null;

		for (Class<?> cl : lookup.value()) {
			if (cl.isEnum()) {
				DataType[] constants = (DataType[]) cl.getEnumConstants();

				for (var edt : constants) {
					if (edt.dataSupport().dataClass().isAssignableFrom(typeClass))
						return edt;
				}
			}
		}

		return null;
	}

	public static boolean matchDataParameters(Method method, Class<?> templateDataClass, DataType dataType) {

		if (dataType == null)
			return false;

		Class<?> dataClass = dataType.dataSupport().dataClass();
		assert dataClass != templateDataClass;

		Class<?>[] dataParameters = extractDataParameters(dataClass);
		Class<?>[] classParameters = method.getParameterTypes();

		var p1 = classParameters;
		var p2 = dataParameters;

		return (p1.length == p2.length)
				&& IntStream.range(0, p1.length)
						.filter(i -> !p1[i].isAssignableFrom(p2[i]))
						.findFirst()
						.isEmpty();
	}

	private static Class<?>[] extractDataParameters(Class<?> templateClass) {

		var methodList = Arrays.stream(templateClass.getDeclaredMethods())
				.filter(m -> (m.getModifiers() & Modifier.STATIC) == 0)
				.toList();

		if (methodList.size() > 1)
			methodList = methodList.stream()
					.filter(m -> m.isAnnotationPresent(DataHandler.class))
					.toList();

		if (methodList.isEmpty())
			throw new IllegalStateException("invalid data handle type, no dynamic data method in type %s"
					.formatted(templateClass.toString()));

		if (methodList.size() > 1)
			throw new IllegalStateException("invalid data handle type, too many data methods in type %s"
					.formatted(templateClass.toString()));

		return methodList.get(0).getParameterTypes();
	}

	public static MethodHandle createMethodHandle(Method method, Object container, Class<?> containerClass) {
		try {
			boolean isStatic = (container == null);
			var methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
			var mh = isStatic
					? MethodHandles.lookup().findStatic(containerClass, method.getName(), methodType)
					: MethodHandles.lookup().findVirtual(containerClass, method.getName(), methodType);

			if (!isStatic)
				mh = mh.bindTo(container);

			return mh;
		} catch (Throwable e) {
			e.printStackTrace();

			throw new IllegalStateException(e);
		}

	}
}
