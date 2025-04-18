package org.springframework.ai.azure.openai;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Configuration
@SuppressWarnings("unused")
public class MockAiTestConfiguration {

	public static final Charset FALLBACK_CHARSET = StandardCharsets.UTF_8;

	public static final String SPRING_AI_API_PATH = "/spring-ai/api";

	@Bean
	MockWebServerFactoryBean mockWebServer(MockMvc mockMvc) {
		MockWebServerFactoryBean factoryBean = new MockWebServerFactoryBean();
		factoryBean.setDispatcher(new MockMvcDispatcher(mockMvc));
		return factoryBean;
	}

	static class MockMvcDispatcher extends Dispatcher {

		private final MockMvc mockMvc;

		MockMvcDispatcher(MockMvc mockMvc) {
			Assert.notNull(mockMvc, "Spring MockMvc must not be null");
			this.mockMvc = mockMvc;
		}

		protected MockMvc getMockMvc() {
			return this.mockMvc;
		}

		@Override
		@SuppressWarnings("all")
		public MockResponse dispatch(RecordedRequest request) {

			try {
				MvcResult result = getMockMvc().perform(requestBuilderFrom(request))
					.andExpect(status().isOk())
					.andReturn();

				MockHttpServletResponse response = result.getResponse();

				return mockResponseFrom(response);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		private RequestBuilder requestBuilderFrom(RecordedRequest request) {

			String requestMethod = request.getMethod();
			String requestPath = resolveRequestPath(request);

			URI uri = URI.create(requestPath);

			Buffer requestBody = request.getBody();

			String content = requestBody.readUtf8();

			return MockMvcRequestBuilders.request(requestMethod, uri).content(content);
		}

		private String resolveRequestPath(RecordedRequest request) {

			String requestPath = request.getPath();
			String pavedRequestPath = StringUtils.hasText(requestPath) ? requestPath : "/";

			return pavedRequestPath.startsWith(SPRING_AI_API_PATH) ? pavedRequestPath
					: SPRING_AI_API_PATH.concat(pavedRequestPath);
		}

		private MockResponse mockResponseFrom(MockHttpServletResponse response) {

			MockResponse mockResponse = new MockResponse();

			for (String headerName : response.getHeaderNames()) {
				String headerValue = response.getHeader(headerName);
				if (StringUtils.hasText(headerValue)) {
					mockResponse.addHeader(headerName, headerValue);
				}
			}

			mockResponse.setResponseCode(response.getStatus());
			mockResponse.setBody(getBody(response));

			return mockResponse;
		}

		private String getBody(MockHttpServletResponse response) {

			Charset responseCharacterEncoding = Charset.forName(response.getCharacterEncoding());

			try {
				return response.getContentAsString(FALLBACK_CHARSET);
			}
			catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Failed to decode content using HttpServletResponse Charset [%s]"
					.formatted(responseCharacterEncoding), e);
			}
		}

	}

	static class MockWebServerFactoryBean implements FactoryBean<MockWebServer>, InitializingBean, DisposableBean {

		private final Logger logger = LoggerFactory.getLogger(getClass().getName());

		private final Queue<MockResponse> queuedResponses = new ConcurrentLinkedDeque<>();

		private Dispatcher dispatcher;

		private MockWebServer mockWebServer;

		protected Optional<Dispatcher> getDispatcher() {
			return Optional.ofNullable(this.dispatcher);
		}

		public void setDispatcher(@Nullable Dispatcher dispatcher) {
			this.dispatcher = dispatcher;
		}

		protected Logger getLogger() {
			return logger;
		}

		@Override
		public MockWebServer getObject() {
			return start(this.mockWebServer);
		}

		@Override
		public Class<?> getObjectType() {
			return MockWebServer.class;
		}

		@Override
		public void afterPropertiesSet() {
			this.mockWebServer = new MockWebServer();
			this.queuedResponses.forEach(this.mockWebServer::enqueue);
			getDispatcher().ifPresent(this.mockWebServer::setDispatcher);
		}

		public MockWebServerFactoryBean enqueue(MockResponse response) {
			Assert.notNull(response, "MockResponse must not be null");
			this.queuedResponses.add(response);
			return this;
		}

		@Override
		public void destroy() {

			try {
				this.mockWebServer.shutdown();
			}
			catch (IOException e) {
				getLogger().warn("MockWebServer was not shutdown correctly: {}", e.getMessage());
				getLogger().trace("MockWebServer shutdown failure", e);
			}
		}

		private MockWebServer start(MockWebServer webServer) {

			try {
				webServer.start();
				return webServer;
			}
			catch (IOException e) {
				throw new IllegalStateException("Failed to start MockWebServer", e);
			}
		}

	}

}
