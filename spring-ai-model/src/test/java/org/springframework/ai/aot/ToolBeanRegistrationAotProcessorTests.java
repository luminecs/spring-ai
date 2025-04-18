package org.springframework.ai.aot;

import org.junit.jupiter.api.Test;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.beans.factory.aot.BeanRegistrationAotContribution;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RegisteredBean;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.lang.Nullable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.aot.hint.predicate.RuntimeHintsPredicates.reflection;

class ToolBeanRegistrationAotProcessorTests {

	private final GenerationContext generationContext = mock();

	private final RuntimeHints runtimeHints = new RuntimeHints();

	@Test
	void shouldSkipNonAnnotatedClass() {
		process(NonTools.class);
		assertThat(this.runtimeHints.reflection().typeHints()).isEmpty();
	}

	@Test
	void shouldProcessAnnotatedClass() {
		process(TestTools.class);
		assertThat(reflection().onType(TestTools.class)).accepts(this.runtimeHints);
	}

	private void process(Class<?> beanClass) {
		when(this.generationContext.getRuntimeHints()).thenReturn(this.runtimeHints);
		BeanRegistrationAotContribution contribution = createContribution(beanClass);
		if (contribution != null) {
			contribution.applyTo(this.generationContext, mock());
		}
	}

	private static @Nullable BeanRegistrationAotContribution createContribution(Class<?> beanClass) {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerBeanDefinition(beanClass.getName(), new RootBeanDefinition(beanClass));
		return new ToolBeanRegistrationAotProcessor()
			.processAheadOfTime(RegisteredBean.of(beanFactory, beanClass.getName()));
	}

	static class TestTools {

		@Tool
		String testTool() {
			return "Testing";
		}

	}

	static class NonTools {

		String nonTool() {
			return "More testing";
		}

	}

}
