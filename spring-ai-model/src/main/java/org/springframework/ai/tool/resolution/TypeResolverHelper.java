package org.springframework.ai.tool.resolution;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.KotlinDetector;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

public final class TypeResolverHelper {

	private TypeResolverHelper() {

	}

	public static Class<?> getConsumerInputClass(Class<? extends Consumer<?>> consumerClass) {
		ResolvableType resolvableType = ResolvableType.forClass(consumerClass).as(Consumer.class);
		return (resolvableType == ResolvableType.NONE ? Object.class : resolvableType.getGeneric(0).toClass());
	}

	public static Class<?> getBiFunctionInputClass(Class<? extends BiFunction<?, ?, ?>> biFunctionClass) {
		return getBiFunctionArgumentClass(biFunctionClass, 0);
	}

	public static Class<?> getFunctionInputClass(Class<? extends Function<?, ?>> functionClass) {
		return getFunctionArgumentClass(functionClass, 0);
	}

	public static Class<?> getFunctionOutputClass(Class<? extends Function<?, ?>> functionClass) {
		return getFunctionArgumentClass(functionClass, 1);
	}

	public static Class<?> getFunctionArgumentClass(Class<? extends Function<?, ?>> functionClass, int argumentIndex) {
		ResolvableType resolvableType = ResolvableType.forClass(functionClass).as(Function.class);
		return (resolvableType == ResolvableType.NONE ? Object.class
				: resolvableType.getGeneric(argumentIndex).toClass());
	}

	public static Class<?> getBiFunctionArgumentClass(Class<? extends BiFunction<?, ?, ?>> biFunctionClass,
			int argumentIndex) {
		ResolvableType resolvableType = ResolvableType.forClass(biFunctionClass).as(BiFunction.class);
		return (resolvableType == ResolvableType.NONE ? Object.class
				: resolvableType.getGeneric(argumentIndex).toClass());
	}

	public static ResolvableType resolveBeanType(GenericApplicationContext applicationContext, String beanName) {
		BeanDefinition beanDefinition = getBeanDefinition(applicationContext, beanName);

		ResolvableType functionType = beanDefinition.getResolvableType();
		if (functionType.resolve() != null) {
			return functionType;
		}

		if (beanDefinition instanceof RootBeanDefinition rootBeanDefinition) {
			return resolveRootBeanDefinitionType(applicationContext, rootBeanDefinition);
		}

		return resolveComponentBeanType(applicationContext, beanDefinition, beanName);
	}

	private static BeanDefinition getBeanDefinition(GenericApplicationContext applicationContext, String beanName) {
		try {
			return applicationContext.getBeanDefinition(beanName);
		}
		catch (NoSuchBeanDefinitionException ex) {
			throw new IllegalArgumentException(
					"Functional bean with name " + beanName + " does not exist in the context.");
		}
	}

	private static ResolvableType resolveRootBeanDefinitionType(GenericApplicationContext applicationContext,
			RootBeanDefinition rootBeanDefinition) {

		Class<?> factoryClass;
		boolean isStatic;

		if (rootBeanDefinition.getFactoryBeanName() != null) {
			factoryClass = applicationContext.getBeanFactory().getType(rootBeanDefinition.getFactoryBeanName());
			isStatic = false;
		}
		else {
			factoryClass = rootBeanDefinition.getBeanClass();
			isStatic = true;
		}

		Assert.state(factoryClass != null, "Unresolvable factory class");
		factoryClass = ClassUtils.getUserClass(factoryClass);

		Method uniqueCandidate = findUniqueFactoryMethod(factoryClass, isStatic, rootBeanDefinition);
		rootBeanDefinition.setResolvedFactoryMethod(uniqueCandidate);
		return rootBeanDefinition.getResolvableType();
	}

	private static Method findUniqueFactoryMethod(Class<?> factoryClass, boolean isStatic,
			RootBeanDefinition rootBeanDefinition) {
		Method[] candidates = getCandidateMethods(factoryClass, rootBeanDefinition);
		Method uniqueCandidate = null;

		for (Method candidate : candidates) {
			if ((!isStatic || isStaticCandidate(candidate, factoryClass))
					&& rootBeanDefinition.isFactoryMethod(candidate)) {
				if (uniqueCandidate == null) {
					uniqueCandidate = candidate;
				}
				else if (isParamMismatch(uniqueCandidate, candidate)) {
					uniqueCandidate = null;
					break;
				}
			}
		}

		return uniqueCandidate;
	}

	private static ResolvableType resolveComponentBeanType(GenericApplicationContext applicationContext,
			BeanDefinition beanDefinition, String beanName) {
		if (beanDefinition.getFactoryMethodName() == null && beanDefinition.getBeanClassName() != null) {
			try {
				return ResolvableType.forClass(
						ClassUtils.forName(beanDefinition.getBeanClassName(), applicationContext.getClassLoader()));
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalArgumentException("Impossible to resolve the type of bean " + beanName, ex);
			}
		}
		throw new IllegalArgumentException("Impossible to resolve the type of bean " + beanName);
	}

	static private Method[] getCandidateMethods(Class<?> factoryClass, RootBeanDefinition mbd) {
		return (mbd.isNonPublicAccessAllowed() ? ReflectionUtils.getUniqueDeclaredMethods(factoryClass)
				: factoryClass.getMethods());
	}

	static private boolean isStaticCandidate(Method method, Class<?> factoryClass) {
		return (Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass() == factoryClass);
	}

	static private boolean isParamMismatch(Method uniqueCandidate, Method candidate) {
		int uniqueCandidateParameterCount = uniqueCandidate.getParameterCount();
		int candidateParameterCount = candidate.getParameterCount();
		return (uniqueCandidateParameterCount != candidateParameterCount
				|| !Arrays.equals(uniqueCandidate.getParameterTypes(), candidate.getParameterTypes()));
	}

	public static ResolvableType getFunctionArgumentType(ResolvableType functionType, int argumentIndex) {

		Class<?> resolvableClass = functionType.toClass();
		ResolvableType functionArgumentResolvableType = ResolvableType.NONE;

		if (Function.class.isAssignableFrom(resolvableClass)) {
			functionArgumentResolvableType = functionType.as(Function.class);
		}
		else if (BiFunction.class.isAssignableFrom(resolvableClass)) {
			functionArgumentResolvableType = functionType.as(BiFunction.class);
		}
		else if (Supplier.class.isAssignableFrom(resolvableClass)) {
			functionArgumentResolvableType = functionType.as(Supplier.class);
		}
		else if (Consumer.class.isAssignableFrom(resolvableClass)) {
			functionArgumentResolvableType = functionType.as(Consumer.class);
		}
		else if (KotlinDetector.isKotlinPresent()) {
			if (KotlinDelegate.isKotlinFunction(resolvableClass)) {
				functionArgumentResolvableType = KotlinDelegate.adaptToKotlinFunctionType(functionType);
			}
			else if (KotlinDelegate.isKotlinBiFunction(resolvableClass)) {
				functionArgumentResolvableType = KotlinDelegate.adaptToKotlinBiFunctionType(functionType);
			}
			else if (KotlinDelegate.isKotlinSupplier(resolvableClass)) {
				functionArgumentResolvableType = KotlinDelegate.adaptToKotlinSupplierType(functionType);
			}
		}

		if (functionArgumentResolvableType == ResolvableType.NONE) {
			throw new IllegalArgumentException(
					"Type must be a Function, BiFunction, Function1 or Function2. Found: " + functionType);
		}

		return functionArgumentResolvableType.getGeneric(argumentIndex);
	}

	private static final class KotlinDelegate {

		public static boolean isKotlinSupplier(Class<?> clazz) {
			return Function0.class.isAssignableFrom(clazz);
		}

		public static ResolvableType adaptToKotlinSupplierType(ResolvableType resolvableType) {
			return resolvableType.as(Function0.class);
		}

		public static boolean isKotlinFunction(Class<?> clazz) {
			return Function1.class.isAssignableFrom(clazz);
		}

		public static ResolvableType adaptToKotlinFunctionType(ResolvableType resolvableType) {
			return resolvableType.as(Function1.class);
		}

		public static boolean isKotlinBiFunction(Class<?> clazz) {
			return Function2.class.isAssignableFrom(clazz);
		}

		public static ResolvableType adaptToKotlinBiFunctionType(ResolvableType resolvableType) {
			return resolvableType.as(Function2.class);
		}

	}

}
