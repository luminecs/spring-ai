package org.springframework.boot.testsupport;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

class DisabledIfProcessUnavailableCondition implements ExecutionCondition {

	private static final String USR_LOCAL_BIN = "/usr/local/bin";

	private static final boolean MAC_OS = System.getProperty("os.name").toLowerCase().contains("mac");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		List<String[]> commands = new ArrayList<>();
		context.getTestClass().map(this::getAnnotationValue).orElse(Stream.empty()).forEach(commands::add);
		context.getTestMethod().map(this::getAnnotationValue).orElse(Stream.empty()).forEach(commands::add);
		try {
			commands.forEach(this::check);
			return ConditionEvaluationResult.enabled("All processes available");
		}
		catch (Throwable ex) {
			return ConditionEvaluationResult.disabled("Process unavailable", ex.getMessage());
		}
	}

	private Stream<String[]> getAnnotationValue(AnnotatedElement testElement) {
		return MergedAnnotations.from(testElement, SearchStrategy.TYPE_HIERARCHY)
			.stream(DisabledIfProcessUnavailable.class)
			.map(annotation -> annotation.getStringArray(MergedAnnotation.VALUE));
	}

	private void check(String[] command) {
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		try {
			Process process = processBuilder.start();
			Assert.isTrue(process.waitFor(30, TimeUnit.SECONDS), "Process did not exit within 30 seconds");
			Assert.state(process.exitValue() == 0, () -> "Process exited with %d".formatted(process.exitValue()));
			process.destroy();
		}
		catch (Exception ex) {
			String path = processBuilder.environment().get("PATH");
			if (MAC_OS && path != null && !path.contains(USR_LOCAL_BIN)
					&& !command[0].startsWith(USR_LOCAL_BIN + "/")) {
				String[] localCommand = command.clone();
				localCommand[0] = USR_LOCAL_BIN + "/" + localCommand[0];
				check(localCommand);
				return;
			}
			throw new RuntimeException(
					"Unable to start process '%s'".formatted(StringUtils.arrayToDelimitedString(command, " ")));
		}
	}

}
