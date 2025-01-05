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
package com.slytechs.jnet.platform.api.util;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The Class Reflections.
 *
 * @author Mark Bednarczyk
 */
public final class Reflections {

	public static class HandleGetter<T> {
		private final Method method;
		private MethodHandle handle;

		public HandleGetter(Method method) throws IllegalAccessException {
			this.method = method;
			this.handle = MethodHandles.lookup()
					.unreflect(method);
		}

		public HandleGetter(Object target, Method method) throws IllegalAccessException {
			method.setAccessible(true);

			this.method = method;
			this.handle = MethodHandles.lookup()
					.unreflect(method)
					.bindTo(target);
		}

		public T get() {
			try {
				return (T) handle.invoke();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}

	}

	/**
	 * Instantiates a new reflections.
	 */
	private Reflections() {}

	/**
	 * Load class.
	 *
	 * @param <T>        the generic type
	 * @param moduleName the module name
	 * @param className  the class name
	 * @return the class
	 * @throws ClassNotFoundException the class not found exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> loadClass(String moduleName, String className) throws ClassNotFoundException {

		if (className == null)
			return null;

		Optional<Module> module = Optional.empty();
		if (moduleName != null)
			module = ModuleLayer.boot()
					.findModule(moduleName);

		Class<T> clazz;
		if (module.isPresent())
			clazz = (Class<T>) Class
					.forName(module.get(), className);
		else
			clazz = (Class<T>) Class
					.forName(className);

		return clazz;
	}

	/**
	 * Load module.
	 *
	 * @param moduleName the module name
	 * @return the module
	 */
	public static Module loadModule(String moduleName) {
		Optional<Module> module = Optional.empty();
		if (moduleName != null)
			module = ModuleLayer.boot()
					.findModule(moduleName);

		return module.orElse(null);
	}

	/**
	 * Load class.
	 *
	 * @param <T>       the generic type
	 * @param module    the module
	 * @param className the class name
	 * @return the class
	 * @throws ClassNotFoundException the class not found exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> loadClass(Module module, String className) throws ClassNotFoundException {

		if (className == null)
			throw new ClassNotFoundException("null classname");

		Class<T> clazz;
		if (module != null)
			clazz = (Class<T>) Class.forName(module, className);
		else
			clazz = (Class<T>) Class.forName(className);

		if (clazz == null)
			throw new ClassNotFoundException("module: %s, classname=%s"
					.formatted(module.getName(), className));

		return clazz;
	}

	public static <T extends Annotation> List<Method> listMethods(Class<?> sourceClass, Class<T> annotationClass,
			Predicate<T> annotationFilter) {
		return listMethods(sourceClass, m -> true
				&& m.isAnnotationPresent(annotationClass)
				&& annotationFilter.test(m.getAnnotation(annotationClass)));
	}

	public static <T extends Annotation> List<Method> listMethods(Class<?> sourceClass, Class<T> annotationClass) {
		return listMethods(sourceClass, m -> m.isAnnotationPresent(annotationClass));
	}

	/**
	 * List methods.
	 *
	 * @param sourceClass the source class
	 * @param filter      the filter
	 * @return the list
	 */
	public static List<Method> listMethods(Class<?> sourceClass, Predicate<Method> filter) {
		return listMethods(sourceClass, filter, new ArrayList<>());
	}

	/**
	 * List methods.
	 *
	 * @param sourceClass the source class
	 * @param filter      the filter
	 * @param list        the list
	 * @return the list
	 */
	public static List<Method> listMethods(Class<?> sourceClass, Predicate<Method> filter, List<Method> list) {
		if (sourceClass == Object.class)
			return list;

		for (var method : sourceClass.getDeclaredMethods()) {
			if (filter.test(method))
				list.add(method);
		}

		return listMethods(sourceClass.getSuperclass(), filter, list);
	}

	public static <T> HandleGetter<T> getter(Object target, Method method)
			throws IllegalAccessException {
		return new HandleGetter<>(target, method);
	}
}
