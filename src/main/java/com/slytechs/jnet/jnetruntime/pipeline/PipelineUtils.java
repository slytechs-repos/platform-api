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
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Utility methods for looking up and computing pipeline elements. This class
 * provides various static methods to support the pipeline framework's
 * reflection and annotation-based operations.
 * 
 * @author Sly Technologies Inc
 * @author repos@slytechs.com
 */
class PipelineUtils {

	private PipelineUtils() {
		/* Do not instantiate */
	}

	/**
	 * Creates a new array of the specified component type and size.
	 *
	 * @param <T>           The component type of the array
	 * @param componentType The class object representing the component type
	 * @param size          The size of the array
	 * @return A new array of the specified type and size
	 */
	public static <T> T[] newArray(Class<T> componentType, int size) {
		@SuppressWarnings("unchecked")
		T[] array = (T[]) Array.newInstance(componentType, size);
		return array;
	}

	/**
	 * Returns a string representation of an enable flag.
	 *
	 * @param b The boolean flag
	 * @return "on" if true, "off" if false
	 */
	public static String enableFlagLabel(boolean b) {
		return b ? "on" : "off";
	}

	/**
	 * Returns a string representation of a bypass flag.
	 *
	 * @param b The boolean flag
	 * @return "bypassed" if true, "exec" if false
	 */
	public static String bypassFlagLabel(boolean b) {
		return b ? "bypassed" : "exec";
	}

	/**
	 * Recursively looks up an annotation on a method or its declaring class.
	 *
	 * @param <T>    The type of the annotation
	 * @param method The method to search for the annotation
	 * @param atype  The class of the annotation to look for
	 * @return The annotation if found, null otherwise
	 */
	public static <T extends Annotation> T lookupAnnotationRecusively(Method method, Class<T> atype) {
		T a = method.getAnnotation(atype);
		if (a == null)
			return lookupAnnotationRecusively(method.getDeclaringClass(), atype);
		return a;
	}

	/**
	 * Recursively looks up an annotation on a class or its superclasses.
	 *
	 * @param <T>   The type of the annotation
	 * @param clazz The class to search for the annotation
	 * @param atype The class of the annotation to look for
	 * @return The annotation if found, null otherwise
	 */
	public static <T extends Annotation> T lookupAnnotationRecusively(Class<?> clazz, Class<T> atype) {
		if (clazz == null)
			return null;
		T a = clazz.getAnnotation(atype);
		if (a == null)
			return lookupAnnotationRecusively(clazz.getSuperclass(), atype);
		return a;
	}

	/**
	 * Looks up a DataType for a given class using a TypeLookup annotation.
	 *
	 * @param typeClass The class to find a DataType for
	 * @param lookup    The TypeLookup annotation containing potential DataType
	 *                  classes
	 * @return The matching DataType if found, null otherwise
	 */
	public static DataType lookupDataType(Class<?> typeClass, ATypeLookup lookup) {
		if (lookup == null)
			return null;
		for (Class<?> cl : lookup.value()) {
			if (cl.isEnum()) {
				DataType[] constants = (DataType[]) cl.getEnumConstants();
				for (var edt : constants) {
					if (edt.dataClass().isAssignableFrom(typeClass))
						return edt;
				}
			}
		}
		return null;
	}

	/**
	 * Checks if a method's parameters match a given data type.
	 *
	 * @param method            The method to check
	 * @param templateDataClass The template data class
	 * @param dataType          The DataType to match against
	 * @return true if the parameters match, false otherwise
	 */
	public static boolean matchDataParameters(Method method, Class<?> templateDataClass, DataType dataType) {
		if (dataType == null)
			return false;
		Class<?> dataClass = dataType.dataClass();
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

	/**
	 * Extracts the parameter types from a data handling method in a class.
	 *
	 * @param templateClass The class to extract parameters from
	 * @return An array of Class objects representing the parameter types
	 * @throws IllegalStateException if no suitable method is found or if multiple
	 *                               methods are found
	 */
	private static Class<?>[] extractDataParameters(Class<?> templateClass) {
		var methodList = Arrays.stream(templateClass.getDeclaredMethods())
				.filter(m -> (m.getModifiers() & Modifier.STATIC) == 0)
				.toList();
		if (methodList.size() > 1)
			methodList = methodList.stream()
					.filter(m -> m.isAnnotationPresent(ADataHandler.class))
					.toList();
		if (methodList.isEmpty())
			throw new IllegalStateException("invalid data handle type, no dynamic data method in type %s"
					.formatted(templateClass.toString()));
		if (methodList.size() > 1)
			throw new IllegalStateException("invalid data handle type, too many data methods in type %s"
					.formatted(templateClass.toString()));
		return methodList.get(0).getParameterTypes();
	}

	/**
	 * Creates a MethodHandle for a given method and container object.
	 *
	 * @param method         The method to create a handle for
	 * @param container      The container object (null for static methods)
	 * @param containerClass The class of the container
	 * @return A MethodHandle for the specified method
	 * @throws IllegalStateException if the method handle cannot be created
	 */
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