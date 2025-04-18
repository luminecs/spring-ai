package org.springframework.ai.retry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResponseErrorHandler;

public abstract class RetryUtils {

	public static final ResponseErrorHandler DEFAULT_RESPONSE_ERROR_HANDLER = new ResponseErrorHandler() {

		@Override
		public boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
			return response.getStatusCode().isError();
		}

		@Override
		public void handleError(@NonNull ClientHttpResponse response) throws IOException {
			if (response.getStatusCode().isError()) {
				String error = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
				String message = String.format("%s - %s", response.getStatusCode().value(), error);

				if (response.getStatusCode().is4xxClientError()) {
					throw new NonTransientAiException(message);
				}
				throw new TransientAiException(message);
			}
		}
	};

	private static final Logger logger = LoggerFactory.getLogger(RetryUtils.class);

	public static final RetryTemplate DEFAULT_RETRY_TEMPLATE = RetryTemplate.builder()
		.maxAttempts(10)
		.retryOn(TransientAiException.class)
		.exponentialBackoff(Duration.ofMillis(2000), 5, Duration.ofMillis(3 * 60000))
		.withListener(new RetryListener() {

			@Override
			public <T extends Object, E extends Throwable> void onError(RetryContext context,
					RetryCallback<T, E> callback, Throwable throwable) {
				logger.warn("Retry error. Retry count:" + context.getRetryCount(), throwable);
			}
		})
		.build();

	public static final RetryTemplate SHORT_RETRY_TEMPLATE = RetryTemplate.builder()
		.maxAttempts(10)
		.retryOn(TransientAiException.class)
		.fixedBackoff(Duration.ofMillis(100))
		.withListener(new RetryListener() {

			@Override
			public <T extends Object, E extends Throwable> void onError(RetryContext context,
					RetryCallback<T, E> callback, Throwable throwable) {
				logger.warn("Retry error. Retry count:" + context.getRetryCount());
			}
		})
		.build();

}
