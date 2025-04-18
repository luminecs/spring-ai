package org.springframework.ai.util;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.Module;

import org.springframework.beans.BeanUtils;
import org.springframework.core.KotlinDetector;
import org.springframework.util.ClassUtils;

public abstract class JacksonUtils {

	@SuppressWarnings("unchecked")
	public static List<Module> instantiateAvailableModules() {
		List<Module> modules = new ArrayList<>();
		try {
			Class<? extends com.fasterxml.jackson.databind.Module> jdk8ModuleClass = (Class<? extends Module>) ClassUtils
				.forName("com.fasterxml.jackson.datatype.jdk8.Jdk8Module", null);
			com.fasterxml.jackson.databind.Module jdk8Module = BeanUtils.instantiateClass(jdk8ModuleClass);
			modules.add(jdk8Module);
		}
		catch (ClassNotFoundException ex) {

		}

		try {
			Class<? extends com.fasterxml.jackson.databind.Module> javaTimeModuleClass = (Class<? extends Module>) ClassUtils
				.forName("com.fasterxml.jackson.datatype.jsr310.JavaTimeModule", null);
			com.fasterxml.jackson.databind.Module javaTimeModule = BeanUtils.instantiateClass(javaTimeModuleClass);
			modules.add(javaTimeModule);
		}
		catch (ClassNotFoundException ex) {

		}

		try {
			Class<? extends com.fasterxml.jackson.databind.Module> parameterNamesModuleClass = (Class<? extends Module>) ClassUtils
				.forName("com.fasterxml.jackson.module.paramnames.ParameterNamesModule", null);
			com.fasterxml.jackson.databind.Module parameterNamesModule = BeanUtils
				.instantiateClass(parameterNamesModuleClass);
			modules.add(parameterNamesModule);
		}
		catch (ClassNotFoundException ex) {

		}

		if (KotlinDetector.isKotlinPresent()) {
			try {
				Class<? extends com.fasterxml.jackson.databind.Module> kotlinModuleClass = (Class<? extends Module>) ClassUtils
					.forName("com.fasterxml.jackson.module.kotlin.KotlinModule", null);
				Module kotlinModule = BeanUtils.instantiateClass(kotlinModuleClass);
				modules.add(kotlinModule);
			}
			catch (ClassNotFoundException ex) {

			}
		}
		return modules;
	}

}
