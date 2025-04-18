package org.springframework.ai.vertexai.gemini.tool;

import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockWeatherService implements Function<MockWeatherService.Request, MockWeatherService.Response> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public Response apply(Request request) {

		double temperature = 0;
		if (request.location().contains("Paris")) {
			temperature = 15;
		}
		else if (request.location().contains("Tokyo")) {
			temperature = 10;
		}
		else if (request.location().contains("San Francisco")) {
			temperature = 30;
		}

		logger.info("Request is {}, response temperature is {}", request, temperature);
		return new Response(temperature, Unit.C);
	}

	public enum Unit {

		C("metric"),

		F("imperial");

		public final String unitName;

		Unit(String text) {
			this.unitName = text;
		}

	}

	@JsonInclude(Include.NON_NULL)
	@JsonClassDescription("Weather API request")
	public record Request(@JsonProperty(required = true,
			value = "location") @JsonPropertyDescription("The city and state e.g. San Francisco, CA") String location,
			@JsonProperty(required = true, value = "unit") @JsonPropertyDescription("Temperature unit") Unit unit) {

	}

	public record Response(double temp, Unit unit) {

	}

}
